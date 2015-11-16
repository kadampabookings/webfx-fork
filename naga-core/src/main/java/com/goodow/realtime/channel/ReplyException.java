/*
 * Note: this code is a fork of Goodow realtime-channel project https://github.com/goodow/realtime-channel
 */

/*
 * Copyright 2014 Goodow.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.goodow.realtime.channel;

/*
 * @author 田传武 (aka larrytin) - author of Goodow realtime-channel project
 * @author Bruno Salmon - fork, refactor & update for the naga project
 *
 * <a href="https://github.com/goodow/realtime-channel/blob/master/src/main/java/com/goodow/realtime/channel/ReplyException.java">Original Goodow class</a>
 */
public class ReplyException extends RuntimeException {
  private static final long serialVersionUID = -4441153344646081242L;
  private final ReplyFailure failureType;
  private final int failureCode;

  public ReplyException(ReplyFailure failureType) {
    super((String) null);
    this.failureType = failureType;
    this.failureCode = -1;
  }

  public ReplyException(ReplyFailure failureType, int failureCode, String message) {
    super(message);
    this.failureType = failureType;
    this.failureCode = failureCode;
  }

  public ReplyException(ReplyFailure failureType, String message) {
    super(message);
    this.failureType = failureType;
    this.failureCode = -1;
  }

  public int failureCode() {
    return failureCode;
  }

  public ReplyFailure failureType() {
    return failureType;
  }
}