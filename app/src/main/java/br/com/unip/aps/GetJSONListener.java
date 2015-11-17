package br.com.unip.aps;

/**
 * Created by Jonatas Cabral on 17/11/2015.
 */
public interface GetJSONListener {
    void onAddNotice(String jsonNotice);
    void onGetNotices(String jsonNotices);
}
