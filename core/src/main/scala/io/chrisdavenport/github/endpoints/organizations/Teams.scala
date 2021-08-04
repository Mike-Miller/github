package io.chrisdavenport.github.endpoints.organizations

import cats.implicits._
import cats.effect._

import org.http4s._
import org.http4s.implicits._

import io.chrisdavenport.github.Auth
import io.chrisdavenport.github.internals.RequestConstructor

import io.chrisdavenport.github.data.Teams._


object Teams {

  // Add or update team repository
  // https://developer.github.com/v3/teams/#add-or-update-team-repository
  def addOrUpdateTeamRepo[F[_]: Concurrent](
    teamId: Int,
    organization: String,
    repoName: String,
    permission: Permission,
    auth: Auth
  ) = {
    import io.chrisdavenport.github.internals.GithubMedia.jsonEncoder
    RequestConstructor.runRequestWithBody[F, AddTeamRepoPermission, Unit](
      auth.some,
      Method.PUT,
      uri"teams" / teamId.toString / "repos" / organization / repoName,
      AddTeamRepoPermission(permission)
    )
  }
}
