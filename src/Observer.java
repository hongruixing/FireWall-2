/**
 * Created by v1ar on 15.01.15.
 */
public interface Observer {
    public void update(int direction, int type, String body);
    public void registerIn(Observable o);
}
