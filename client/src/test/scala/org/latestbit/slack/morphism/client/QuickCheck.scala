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

import java.time.Instant
import java.util.concurrent.locks.ReentrantLock

import org.scalatest.flatspec.AnyFlatSpec
import cats._
import cats.syntax._
import cats.implicits._
import cats.effect._
import cats.effect.concurrent.{ MVar, Semaphore }
import cats.effect.implicits._
import org.latestbit.slack.morphism.concurrent.UniqueLockMonitor

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success, Using }
import org.scalacheck._

class QuickCheck extends AnyFlatSpec {}
