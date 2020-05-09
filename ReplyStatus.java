public class ReplyStatus{
	boolean isDone;
	long timeOut;

	public ReplyStatus(){
		isDone = true;
	}

	public void setDone(){
		isDone = true;
	}
	public void startWaiting(long waitTime){
		isDone = false;
		timeOut = System.currentTimeMillis();
	}

}