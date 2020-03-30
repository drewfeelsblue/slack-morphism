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

package org.latestbit.slack.morphism.client

import org.latestbit.slack.morphism.client.ratectrl.SlackApiRateThrottler
import org.latestbit.slack.morphism.client.reqresp.test.SlackApiTestRequest
import org.scalatest.flatspec.AsyncFlatSpec

class AsyncFutureHttpSttpBackendTests extends AsyncFlatSpec with SlackApiClientTestsSuiteSupport {
  "A Slack client" should "able to try to connect using a async http client network sttp backend" in {
    import cats.instances.future._
    import sttp.client.asynchttpclient.future.AsyncHttpClientFutureBackend

    implicit val sttpBackend = AsyncHttpClientFutureBackend()

    // Creating it with create factory method
    val slackApiClient = SlackApiClient.create()

    // Creating it with throttler
    {
      val _ = SlackApiClient.build.withThrottler( SlackApiRateThrottler.createStandardThrottler() ).create()
    }

    slackApiClient.api.test( SlackApiTestRequest() ).map {
      case Right( resp )                     => fail( s"Unexpected resp: ${resp}" )
      case Left( ex: SlackApiResponseError ) => assert( ex.errorCode !== null )
      case Left( ex )                        => fail( ex )
    }

  }

  it should "able to try to connect using a async http cats effect network sttp backend" in {
    import cats.effect._
    import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend
    implicit val cs: ContextShift[IO] = IO.contextShift( scala.concurrent.ExecutionContext.global )

    AsyncHttpClientCatsBackend[IO]()
      .flatMap { implicit backEnd =>
        for {
          client <- IO( SlackApiClient.create[IO]() )
          result <- client.api.test( SlackApiTestRequest() )
        } yield result
      }
      .unsafeToFuture()
      .map {
        case Right( resp )                     => fail( s"Unexpected resp: ${resp}" )
        case Left( ex: SlackApiResponseError ) => assert( ex.errorCode !== null )
        case Left( ex )                        => fail( ex )
      }

  }

  it should "able to try to connect using a async http cats effect network sttp backend with a throttler" in {
    import cats.effect._

    import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend
    implicit val cs: ContextShift[IO] = IO.contextShift( scala.concurrent.ExecutionContext.global )

    AsyncHttpClientCatsBackend[IO]()
      .flatMap { implicit backEnd =>
        for {
          client <- IO(
                     SlackApiClient
                       .build[IO]
                       .withThrottler( SlackApiRateThrottler.createStandardThrottler() )
                       .create()
                   )
          result <- client.api.test( SlackApiTestRequest() )
        } yield result
      }
      .unsafeToFuture()
      .map {
        case Right( resp )                     => fail( s"Unexpected resp: ${resp}" )
        case Left( ex: SlackApiResponseError ) => assert( ex.errorCode !== null )
        case Left( ex )                        => fail( ex )
      }

  }

}