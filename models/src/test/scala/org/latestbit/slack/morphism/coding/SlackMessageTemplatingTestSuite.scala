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

package org.latestbit.slack.morphism.coding

import org.latestbit.slack.morphism.client.models.messages._
import org.latestbit.slack.morphism.client.templating.SlackMessageTemplate
import org.scalatest.flatspec.AnyFlatSpec

class SlackMessageTemplatingTestSuite extends AnyFlatSpec {

  "SlackMessageTemplate" should "be available to create simple instance" in {
    new SlackMessageTemplate {
      override def renderPlainText(): String = "Test template"
    }
  }

  "it" should "provide DSL to build blocks API" in {
    val testCond = 0

    new SlackMessageTemplate {

      override def renderPlainText(): String = "Test template"

      override def renderBlocks(): Option[List[SlackBlock]] =
        blocksGroup {
          blocksGroup {
            blocks {
              block( dividerBlock() )
              block( sectionBlock( text = md"Test: ${testCond}" ) )
              optBlock( testCond > 0 )( dividerBlock() )
              block(
                sectionBlock(
                  text = md"Test",
                  fields = sectionFields {
                    sectionField( md"Test 1" )
                    sectionField( md"Test 2" )
                    optSectionField( testCond > 0 )( md"Test 3" )
                  },
                  accessory = blockEl(
                    image( "https://example.net/image.png" )
                  )
                )
              )
              block(
                contextBlock(
                  elements = blockElements(
                    blockEl(
                      button( text = plain"test button", action_id = "-" )
                    ),
                    blockEl(
                      button( text = plain"test button", action_id = "-" )
                    )
                  )
                )
              )
              block(
                sectionBlock(
                  text = md"Test 2",
                  accessory = blockEl(
                    overflowMenu(
                      action_id = "-",
                      options = menuItems(
                        menuItem( menuItemValue( text = plain"test-menu-item", value = "" ) )
                      )
                    )
                  )
                )
              )
              block(
                contextBlock(
                  elements = blockElements(
                    blockEl(
                      staticMenu(
                        placeholder = plain"test",
                        action_id = "-",
                        options = staticMenuItems(
                          menuItem( menuItemValue( text = plain"test-menu-item", value = "" ) ),
                          optMenuItem( testCond > 0 )( menuItemValue( text = plain"test-menu-item2", value = "" ) )
                        ),
                        confirm = confirm(
                          confirmValue(
                            title = plain"Test title",
                            text = md"Confirm this",
                            confirm = plain"OK",
                            deny = plain"Cancel"
                          )
                        )
                      )
                    )
                  )
                )
              )
            }
          }
          blocksGroup {
            blocks {
              block( dividerBlock() )
            }
          }
        }
    }

  }

}