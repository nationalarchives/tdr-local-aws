package uk.gov.nationalarchives.tdr.localaws.backendchecks

import java.nio.file._

import com.typesafe.config.ConfigFactory
import graphql.codegen.AddAntivirusMetadata.AddAntivirusMetadata
import graphql.codegen.GetOriginalPath.getOriginalPath
import uk.gov.nationalarchives.tdr.GraphQLClient
import uk.gov.nationalarchives.tdr.localaws.backendchecks.auth.TokenService
import uk.gov.nationalarchives.tdr.localaws.backendchecks.checks.AntivirusCheck

import scala.concurrent.ExecutionContext.Implicits.global

object FakeBackendChecker extends App {

  private val config = ConfigFactory.load

  private val apiUrl = config.getString("api.baseUrl")
  private val getDocumentClient = new GraphQLClient[getOriginalPath.Data, getOriginalPath.Variables](apiUrl)
  private val antivirusClient = new GraphQLClient[AddAntivirusMetadata.Data, AddAntivirusMetadata.Variables](apiUrl)

  private val tokenService = new TokenService(config)
  private val antivirusChecker = new AntivirusCheck(tokenService, getDocumentClient, antivirusClient)

  private val parentDirectory = Paths.get(config.getString("files.s3UploadDirectory"))

  private val fileWatcher = new FileWatcher(parentDirectory, antivirusChecker)
  fileWatcher.startWatching
}
