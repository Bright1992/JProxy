package exceptions;


public class InvalidConfigFormatException extends Exception {
    public InvalidConfigFormatException(String msg){
        super(msg);
    }
    public InvalidConfigFormatException(){
        super();
    }
}
