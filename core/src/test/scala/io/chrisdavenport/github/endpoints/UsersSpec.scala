package io.chrisdavenport.github.endpoints

import cats.effect._
import cats.effect.testing.specs2.CatsEffect

import io.circe.parser._

import io.chrisdavenport.github.OAuth

import org.http4s._
import org.http4s.client._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.implicits._

import org.specs2.mutable.Specification

class UsersSpec extends Specification with CatsEffect {

  "Users" should {
  val getSingleUser : HttpRoutes[IO] = HttpRoutes.of {
    case GET -> Root / "users" / _ /*username*/ =>
      val json = parse("""
      {
  "login": "octocat",
  "id": 1,
  "node_id": "MDQ6VXNlcjE=",
  "avatar_url": "https://github.com/images/error/octocat_happy.gif",
  "gravatar_id": "",
  "url": "https://api.github.com/users/octocat",
  "html_url": "https://github.com/octocat",
  "followers_url": "https://api.github.com/users/octocat/followers",
  "following_url": "https://api.github.com/users/octocat/following{/other_user}",
  "gists_url": "https://api.github.com/users/octocat/gists{/gist_id}",
  "starred_url": "https://api.github.com/users/octocat/starred{/owner}{/repo}",
  "subscriptions_url": "https://api.github.com/users/octocat/subscriptions",
  "organizations_url": "https://api.github.com/users/octocat/orgs",
  "repos_url": "https://api.github.com/users/octocat/repos",
  "events_url": "https://api.github.com/users/octocat/events{/privacy}",
  "received_events_url": "https://api.github.com/users/octocat/received_events",
  "type": "User",
  "site_admin": false,
  "name": "monalisa octocat",
  "company": "GitHub",
  "blog": "https://github.com/blog",
  "location": "San Francisco",
  "email": "octocat@github.com",
  "hireable": false,
  "bio": "There once was...",
  "public_repos": 2,
  "public_gists": 1,
  "followers": 20,
  "following": 0,
  "created_at": "2008-01-14T04:33:35Z",
  "updated_at": "2008-01-14T04:33:35Z"
}
      """).toOption.get
      Ok(json)
  }

    "return valid for github expected response" in {
      Users.userInfoFor[IO]("anything", None)
        .run(Client.fromHttpApp(getSingleUser.orNotFound))
        .attempt
        .map{_ must beRight}
    }

      val getAuthenticatedUser = HttpRoutes.of[IO]{
    case GET -> Root / "user" =>
      val json = parse("""
        {
          "login": "octocat",
          "id": 1,
          "node_id": "MDQ6VXNlcjE=",
          "avatar_url": "https://github.com/images/error/octocat_happy.gif",
          "gravatar_id": "",
          "url": "https://api.github.com/users/octocat",
          "html_url": "https://github.com/octocat",
          "followers_url": "https://api.github.com/users/octocat/followers",
          "following_url": "https://api.github.com/users/octocat/following{/other_user}",
          "gists_url": "https://api.github.com/users/octocat/gists{/gist_id}",
          "starred_url": "https://api.github.com/users/octocat/starred{/owner}{/repo}",
          "subscriptions_url": "https://api.github.com/users/octocat/subscriptions",
          "organizations_url": "https://api.github.com/users/octocat/orgs",
          "repos_url": "https://api.github.com/users/octocat/repos",
          "events_url": "https://api.github.com/users/octocat/events{/privacy}",
          "received_events_url": "https://api.github.com/users/octocat/received_events",
          "type": "User",
          "site_admin": false,
          "name": "monalisa octocat",
          "company": "GitHub",
          "blog": "https://github.com/blog",
          "location": "San Francisco",
          "email": "octocat@github.com",
          "hireable": false,
          "bio": "There once was...",
          "public_repos": 2,
          "public_gists": 1,
          "followers": 20,
          "following": 0,
          "created_at": "2008-01-14T04:33:35Z",
          "updated_at": "2008-01-14T04:33:35Z",
          "private_gists": 81,
          "total_private_repos": 100,
          "owned_private_repos": 100,
          "disk_usage": 10000,
          "collaborators": 8,
          "two_factor_authentication": true,
          "plan": {
            "name": "Medium",
            "space": 400,
            "private_repos": 20,
            "collaborators": 0
          }
        }
        """).toOption.get
      Ok().map(_.withEntity(json))
    }

    "return valid for currently authenticated user info" in {
      Users.userInfoAuthenticatedUser[IO](OAuth("anything"))
        .run(Client.fromHttpApp(getAuthenticatedUser.orNotFound))
        .attempt
        .map{_ must beRight}
    }

  val updateAuthenticatedUser = HttpRoutes.of[IO]{
    case PATCH -> Root / "user" =>
      val json = parse("""
        {
          "login": "octocat",
          "id": 1,
          "node_id": "MDQ6VXNlcjE=",
          "avatar_url": "https://github.com/images/error/octocat_happy.gif",
          "gravatar_id": "",
          "url": "https://api.github.com/users/octocat",
          "html_url": "https://github.com/octocat",
          "followers_url": "https://api.github.com/users/octocat/followers",
          "following_url": "https://api.github.com/users/octocat/following{/other_user}",
          "gists_url": "https://api.github.com/users/octocat/gists{/gist_id}",
          "starred_url": "https://api.github.com/users/octocat/starred{/owner}{/repo}",
          "subscriptions_url": "https://api.github.com/users/octocat/subscriptions",
          "organizations_url": "https://api.github.com/users/octocat/orgs",
          "repos_url": "https://api.github.com/users/octocat/repos",
          "events_url": "https://api.github.com/users/octocat/events{/privacy}",
          "received_events_url": "https://api.github.com/users/octocat/received_events",
          "type": "User",
          "site_admin": false,
          "name": "monalisa octocat",
          "company": "GitHub",
          "blog": "https://github.com/blog",
          "location": "San Francisco",
          "email": "octocat@github.com",
          "hireable": false,
          "bio": "There once was...",
          "public_repos": 2,
          "public_gists": 1,
          "followers": 20,
          "following": 0,
          "created_at": "2008-01-14T04:33:35Z",
          "updated_at": "2008-01-14T04:33:35Z",
          "private_gists": 81,
          "total_private_repos": 100,
          "owned_private_repos": 100,
          "disk_usage": 10000,
          "collaborators": 8,
          "two_factor_authentication": true,
          "plan": {
            "name": "Medium",
            "space": 400,
            "private_repos": 20,
            "collaborators": 0
          }
        }""").toOption.get
      Ok().map(_.withEntity(json))
  }

  "return valid for currently authenticated user info update" in {
    Users.updateAuthenticatedUser[IO](OAuth("anything"), None, Some("zasdfa"), None, None, None, None, None)
      .run(Client.fromHttpApp(updateAuthenticatedUser.orNotFound))
      .attempt
      .map{
        _ must beRight
      }
  }

  val getAllUsers = HttpRoutes.of[IO]{
    case GET -> Root / "users" =>
      val json = parse("""
      [
  {
    "login": "octocat",
    "id": 1,
    "node_id": "MDQ6VXNlcjE=",
    "avatar_url": "https://github.com/images/error/octocat_happy.gif",
    "gravatar_id": "",
    "url": "https://api.github.com/users/octocat",
    "html_url": "https://github.com/octocat",
    "followers_url": "https://api.github.com/users/octocat/followers",
    "following_url": "https://api.github.com/users/octocat/following{/other_user}",
    "gists_url": "https://api.github.com/users/octocat/gists{/gist_id}",
    "starred_url": "https://api.github.com/users/octocat/starred{/owner}{/repo}",
    "subscriptions_url": "https://api.github.com/users/octocat/subscriptions",
    "organizations_url": "https://api.github.com/users/octocat/orgs",
    "repos_url": "https://api.github.com/users/octocat/repos",
    "events_url": "https://api.github.com/users/octocat/events{/privacy}",
    "received_events_url": "https://api.github.com/users/octocat/received_events",
    "type": "User",
    "site_admin": false
  }
]""").toOption.get
      Ok(json)

  }

  "return valid for get All Users" in {
    Users.getAllUsers[IO](None, None)
      .run(Client.fromHttpApp(getAllUsers.orNotFound))
      .compile
      .toList
      .attempt
      .map{_ must beRight}
  }

  }

}
