package zhou.hao.BinanSocket.client.security;

import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utility class to sign messages using HMAC-SHA256.
 */
public class HmacSHA256Signer {

  /**
   * Sign the given message using the given secret.
   * @param message message to sign
   * @param secret secret key
   * @return a signed message
   */
  public static String sign(String message, String secret) {
    try {
      Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
      SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
      sha256_HMAC.init(secretKeySpec);
      return new String(Hex.encodeHex(sha256_HMAC.doFinal(message.getBytes())));
    } catch (Exception e) {
      throw new RuntimeException("Unable to sign message.", e);
    }
  }
  
  public static void main(String[] args) throws Exception{
	  System.out.println(HmacSHA256Signer.sign("symbol=LTCBTC&side=BUY&type=LIMIT&timeInForce=GTC&quantity=1&price=0.1&recvWindow=5000&timestamp=1499827319559", 
			  "NhqPtmdSJYdKjVHjA7PZj4Mge3R5YNiP1e3UZjInClVN65XAbvqqM6A7H5fATj0j"));
  }
}
