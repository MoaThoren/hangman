package server.net;

class MessageException extends Exception {
    private String message;

    MessageException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
