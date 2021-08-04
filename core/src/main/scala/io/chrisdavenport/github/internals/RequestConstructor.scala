package io.chrisdavenport.github.internals

import cats._
import cats.effect._
import cats.implicits._
import fs2._
import org.http4s.implicits._
import org.http4s.client.Client
import org.http4s.headers.Authorization
import _root_.io.chrisdavenport.github._
import cats.data.Kleisli
import org.http4s._
import org.typelevel.ci._

import scala.util.matching.Regex


object RequestConstructor {

  def runRequestWithNoBody[F[_]: Concurrent, B]
  (
    auth: Option[Auth],
    method: Method,
    extendedUri: Uri,
  )(implicit B: EntityDecoder[F, B]): Kleisli[F, Client[F], B] =
    runRequest[F, Unit, B](auth, method, extendedUri, None)

  def runRequestWithBody[F[_]: Concurrent, A, B](
    auth: Option[Auth],
    method: Method,
    extendedUri: Uri,
    body: A
  )(implicit A: EntityEncoder[F, A], B: EntityDecoder[F, B]): Kleisli[F, Client[F], B] =
    runRequest[F, A, B](auth, method, extendedUri, body.some)

  def runRequest[F[_]: Concurrent, A, B](
    auth: Option[Auth],
    method: Method,
    extendedUri: Uri,
    body: Option[A]
  )(implicit A: EntityEncoder[F, A], B: EntityDecoder[F, B]):  Kleisli[F, Client[F], B] = Kleisli{ c =>
    val uri = Uri.resolve(baseUrl(auth), extendedUri)
    val baseReq = Request[F](method = method, uri = uri)
      .withHeaders(extraHeaders)
    val req2 = auth.fold(baseReq)(setAuth(_)(baseReq))
    val req = body.fold(req2)(a => req2.withEntity(a))
    c.expectOr[B](req)(resp => 
      
      resp.bodyText.compile.string.flatMap(body => 
        Concurrent[F].raiseError(new GithubError(resp.status, body))
      )
    )
  }

  final class GithubError private[github] (val status: Status, val body: String) extends Exception(
    s"Github Error Occured - Status: $status Body: $body"
  ) {

    override def equals(obj: Any): Boolean =
      obj match {
        case other: GithubError if other.status === status && other.body === body => true
        case _ => false
      }

  }

  private val RE_LINK: Regex = "[\\s]*<(.*)>; rel=\"(.*)\"".r

  private def getNextUri[F[_]: MonadThrow](r: Response[F]): Option[Uri] = {
    r.headers.headers.flatMap {
      case Header.Raw(name, value) if name == CIString("Link") => value.split(",").toList
      case _ => Nil
    }.collectFirstSome {
      case RE_LINK(uri, "next") => Uri.fromString(uri).toOption
      case _ => None
    }
  } 

  def runPaginatedRequest[F[_]: Concurrent, B](
    auth: Option[Auth],
    extendedUri: Uri,
  )(implicit E: EntityDecoder[F, B]):  Kleisli[({ type S[A] = Stream[F, A]})#S, Client[F], B] = Kleisli{ c =>
    val uri = Uri.resolve(baseUrl(auth), extendedUri)
    unfoldLoopEval(uri){ uri =>
      val baseReq = Request[F](method = Method.GET, uri = uri)
        .withHeaders(extraHeaders)
      val req = auth.fold(baseReq)(setAuth(_)(baseReq))
      c.run(req).use{ resp =>
        if(resp.status.isSuccess)
          resp.as[B].map {
            (_, getNextUri(resp))
          }
        else
          resp.bodyText.compile.string.flatMap(body => 
            Concurrent[F].raiseError[(B, Option[Uri])](new GithubError(resp.status, body))
          )
      }
    }
  }


  //These two will be in the next version of fs2
  
  /** Like [[unfoldLoop]], but takes an effectful function. */
  private def unfoldLoopEval[F[_], S, O](s: S)(f: S => F[(O, Option[S])]): Stream[F, O] =
    Pull
      .loop[F, O, S](
        s =>
          Pull.eval(f(s)).flatMap {
            case (o, sOpt) =>
              Pull.output1(o) >> Pull.pure(sOpt)
          }
      )(s)
      .void
      .stream

  private val GITHUB_URI: Uri = uri"https://api.github.com/"

  private def baseUrl(auth: Option[Auth]): Uri = auth match {
    case Some(EnterpriseOAuth(apiEndpoint, _)) => apiEndpoint
    case _ => GITHUB_URI
  }

  private def setAuth[F[_]](auth: Auth)(req: Request[F]): Request[F] = auth match{
    case BasicAuth(username, password) => 
      req.withHeaders(Authorization(BasicCredentials(username, password)))
    case EnterpriseOAuth(_, token) =>
      req.withHeaders(Authorization(Credentials.Token(AuthScheme.Bearer, token)))
    case OAuth(token) =>
      req.withHeaders(Authorization(Credentials.Token(AuthScheme.Bearer, token)))
  }

  private val extraHeaders: Headers = Headers(
    ("User-Agent", "github.scala/" ++ _root_.io.chrisdavenport.github.BuildInfo.version),
    ("Accept", "application/vnd.github.v3+json")
  )

}