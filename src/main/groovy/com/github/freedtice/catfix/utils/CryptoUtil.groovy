package com.github.freedtice.catfix.utils

import java.security.DigestInputStream
import java.security.MessageDigest

/**
 * @author yuantong
 */
public class CryptoUtil {
  public static String generateMD5(File file) {
    file.withInputStream {
      new DigestInputStream(it, MessageDigest.getInstance('MD5')).withStream {
        it.eachByte {}
        it.messageDigest.digest().encodeHex() as String
      }
    }
  }
}
