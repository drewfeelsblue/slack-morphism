/*
 * Copyright 2019 Abdulla Abdurakhmanov (abdulla@latestbit.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package org.latestbit.slack.morphism.examples.akka.routes

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.ActorContext
import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import akka.stream.typed.scaladsl.ActorMaterializer
import com.typesafe.scalalogging._
import org.latestbit.slack.morphism.events._
import org.latestbit.slack.morphism.examples.akka.AppConfig

import scala.concurrent.ExecutionContext
import io.circe.parser._
import org.latestbit.slack.morphism.client.SlackApiClient
import org.latestbit.slack.morphism.client.reqresp.views.SlackApiViewsPublishRequest
import org.latestbit.slack.morphism.examples.akka.db.SlackTokensDb
import org.latestbit.slack.morphism.examples.akka.templates.SlackHomeTabBlocksTemplateExample
import org.latestbit.slack.morphism.views.SlackHomeView

class SlackPushEventsRoute(
    implicit ctx: ActorContext[_],
    materializer: ActorMaterializer,
    config: AppConfig,
    slackApiClient: SlackApiClient,
    slackTokensDb: ActorRef[SlackTokensDb.Command]
) extends StrictLogging
    with AkkaHttpServerRoutesSupport
    with Directives {

  implicit val ec: ExecutionContext = ctx.system.executionContext

  val routes: Route = {
    path( "push" ) {
      post {
        extractSlackSignedRequest { requestBody =>
          decode[SlackPushEvent]( requestBody ) match {
            case Right( event ) => onEvent( event )
            case Left( ex ) => {
              logger.error( s"Can't decode push event from Slack: ${ex.toString}\n${requestBody}" )
              complete( StatusCodes.BadRequest )
            }
          }
        }
      }
    }
  }

  def onEvent( event: SlackPushEvent ): Route = event match {
    case urlVerEv: SlackUrlVerificationEvent => {
      logger.info( s"Received a challenge request:\n${urlVerEv.challenge}" )
      complete(
        StatusCodes.OK,
        HttpEntity(
          ContentTypes.`text/plain(UTF-8)`,
          urlVerEv.challenge
        )
      )
    }
    case callbackEvent: SlackEventCallback => {
      callbackEvent.event match {
        case body: SlackAppHomeOpenedEvent => {
          logger.info( s"User opened home: ${body}" )
          routeWithSlackApiToken( callbackEvent.team_id ) { implicit slackApiToken =>
            onSuccess(
              slackApiClient.views.publish(
                SlackApiViewsPublishRequest(
                  user_id = body.user,
                  view = SlackHomeView(
                    blocks = new SlackHomeTabBlocksTemplateExample(
                      userId = body.user
                    ).renderBlocks()
                  )
                )
              )
            ) {
              case Right( publishResp ) => {
                logger.info( s"Home view for ${body.user} has been published: ${publishResp}" )
                complete( StatusCodes.OK )
              }
              case Left( err ) => {
                logger.error( s"Unable to update home view for ${body.user}", err )
                complete( StatusCodes.InternalServerError )
              }
            }
          }
        }
        case unknownBody: SlackEventCallbackBody => {
          logger.warn( s"Unsupported callback event received: ${unknownBody}" )
          complete( StatusCodes.OK )
        }
      }
    }

    case pushEvent: SlackPushEvent => {
      logger.warn( s"Unsupported push event received: ${pushEvent}" )
      complete( StatusCodes.OK )
    }
  }

}
