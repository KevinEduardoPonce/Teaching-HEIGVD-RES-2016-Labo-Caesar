package ch.heigvd.res.caesar.protocol;
/**
 *
 * @author Kevin
 */
public class CipherMessage {
    
    //Cipher the message
    public static String cipher(String message,int shift)
    {
        String messageCipher="";
        if(message == null)
        {
            return null;
        }
        for(int i = 0; i < message.length(); i++) {
            char car = message.charAt(i);
            car += shift;
            messageCipher += (char)car;
        }
        return messageCipher;
    }
    
    //Decipher the message
    public static String deCipher(String messageCipher,int shift)
    {
        String message="";
        if(messageCipher == null)
        {
            return null;
        }
        for(int i = 0; i < messageCipher.length(); i++) {
            char car = messageCipher.charAt(i);
            car -= shift;
            message += (char)car;
        }
        return message;
    }
    
}
