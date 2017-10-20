package client.controller.web.inputController.actions;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class testes {

   private static final String REGEX = "[A-Z][1-40]";
   private static final String INPUT = "3";

   public static void main( String args[] ) {
      //Pattern p = Pattern.compile(REGEX);
      //Matcher m = p.matcher(INPUT);   // get a matcher object


      
         System.out.println(INPUT.matches(REGEX));
      
   }
}