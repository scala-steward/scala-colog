/*
 * Copyright (c) 2018 A. Alonso Dominguez
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package colog.slf4j

import cats.effect.Sync

import colog.{LogRecord, Logger, Severity}

import org.slf4j.{LoggerFactory, Logger => JLogger}

object Slf4jLoggers {

  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  def apply[F[_]](baseLogger: JLogger)(implicit F: Sync[F]): Logger[F, LogRecord] = {
    def unsafeLog(record: LogRecord)(f: (String, Throwable) => Unit): F[Unit] =
      F.delay(f(record.message, record.error.orNull))

    Logger { record =>
      val logFn: (String, Throwable) => Unit = record.severity match {
        case Severity.Debug   => baseLogger.debug
        case Severity.Info    => baseLogger.info
        case Severity.Warning => baseLogger.warn
        case Severity.Error   => baseLogger.error
      }
      unsafeLog(record)(logFn)
    }
  }

  def forName[F[_]: Sync](name: String): Logger[F, LogRecord] =
    apply[F](LoggerFactory.getLogger(name))

  def forClass[F[_]: Sync](clazz: Class[_]): Logger[F, LogRecord] =
    apply[F](LoggerFactory.getLogger(clazz))

}
