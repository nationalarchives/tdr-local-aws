package uk.gov.nationalarchives.tdr.localaws.backendchecks

import java.nio.file._

import com.typesafe.config.ConfigFactory
import graphql.codegen.AddAntivirusMetadata.addAntivirusMetadata
import graphql.codegen.AddFFIDMetadata.addFFIDMetadata
import graphql.codegen.AddFileMetadata.addFileMetadata
import graphql.codegen.GetOriginalPath.getOriginalPath
import uk.gov.nationalarchives.tdr.GraphQLClient
import uk.gov.nationalarchives.tdr.localaws.backendchecks.api.FileService
import uk.gov.nationalarchives.tdr.localaws.backendchecks.auth.TokenService
import uk.gov.nationalarchives.tdr.localaws.backendchecks.checks.{AntivirusCheck, ChecksumCheck, FileFormatCheck}

import scala.concurrent.ExecutionContext.Implicits.global

object FakeBackendChecker extends App {

  private val config = ConfigFactory.load

  private val apiUrl = config.getString("api.baseUrl")
  private val getDocumentClient = new GraphQLClient[getOriginalPath.Data, getOriginalPath.Variables](apiUrl)
  private val antivirusClient = new GraphQLClient[addAntivirusMetadata.Data, addAntivirusMetadata.Variables](apiUrl)
  private val addMetadataClient = new GraphQLClient[addFileMetadata.Data, addFileMetadata.Variables](apiUrl)
  private val addFileFormatClient = new GraphQLClient[addFFIDMetadata.Data, addFFIDMetadata.Variables](apiUrl)

  private val tokenService = new TokenService(config)

  private val fileService = new FileService(getDocumentClient, antivirusClient, addMetadataClient, addFileFormatClient)

  private val antivirusChecker = new AntivirusCheck(tokenService, fileService)
  private val checksumCheck = new ChecksumCheck(tokenService, fileService)
  private val fileFormatCheck = new FileFormatCheck(tokenService, fileService)

  private val parentDirectory = Paths.get(config.getString("files.s3UploadDirectory"))

  private val fileWatcher = new FileWatcher(
    parentDirectory,
    Seq(antivirusChecker, checksumCheck, fileFormatCheck)
  )
  fileWatcher.startWatching
}
