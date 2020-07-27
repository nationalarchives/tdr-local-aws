package uk.gov.nationalarchives.tdr.localaws.backendchecks.api

import com.nimbusds.oauth2.sdk.token.BearerAccessToken
import sangria.ast.Document
import uk.gov.nationalarchives.tdr.GraphQLClient

import scala.concurrent.{ExecutionContext, Future}

object GraphQl {
  def sendGraphQlRequest[Data, Variables](
                                           client: GraphQLClient[Data, Variables],
                                           token: BearerAccessToken,
                                           queryVariables: Variables,
                                           graphQlDocument: Document
                                         )(implicit executionContext: ExecutionContext): Future[Data] = {
    client.getResult(token, graphQlDocument, Some(queryVariables)).map(data => {
      data.data match {
        case Some(metadata) => metadata
        case None => throw new RuntimeException(s"Error in GraphQL response: ${data.errors}")
      }
    })
  }
}
