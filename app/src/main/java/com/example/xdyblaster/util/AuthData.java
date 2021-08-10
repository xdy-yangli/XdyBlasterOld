package com.example.xdyblaster.util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

//import static com.baidu.location.h.b.e;

public class AuthData {
    public String cwxx = "1";
    public String sqrq = "20200101";
    public String sbbh = "0";

    public List<Zbqy> zbqyList = new ArrayList<>();
    public List<Jbqy> jbqyList = new ArrayList<>();
//    public String zbqyjd = "0";
//    public String zbqywd = "0";
//    public String zbqybj = "0";
//    public String zbqssj = "0";
//    public String zbqzsj = "0";

//    public String jbqyjd = "0";
//    public String jbqywd = "0";
//    public String jbqybj = "0";
//    public String jbqssj = "0";
//    public String jbqzsj = "0";

    public String htid = "100";
    public String xmbh = "100";
    public String dwdm = "100";
    public ArrayList<LgData> lgDatas = null;
    boolean dataErr = false;

    public AuthData(String data) {
        lgDatas = new ArrayList<>();
        getAthFileData(data);
    }

    public void getAthFileData(String data) {
        int i;
        JSONObject fullJSONObject = null;
        try {
            JSONObject all = new JSONObject(data);
            htid = all.getString("htid");
            xmbh = all.getString("xmbh");
            dwdm = all.getString("dwdm");
            fullJSONObject = all.getJSONObject("lgxx");
            cwxx = getStringFromJson(fullJSONObject, "cwxx");
            sqrq = getStringFromJson(fullJSONObject, "sqrq");
            JSONArray sbbhArray = fullJSONObject.getJSONArray("sbbhs");
            sbbh = getStringFromJson(sbbhArray.getJSONObject(0), "sbbh");
            JSONObject lgs = fullJSONObject.getJSONObject("lgs");
            JSONArray lgArray = lgs.getJSONArray("lg");
            JSONObject lg;
            LgData lgData;
            lgDatas.clear();
            for (i = 0; i < lgArray.length(); i++) {
                lg = lgArray.getJSONObject(i);
                lgData = new LgData();
                lgData.fbh = getStringFromJson(lg, "fbh");
                lgData.uid = getStringFromJson(lg, "uid");
                lgData.gzm = getStringFromJson(lg, "gzm");
                lgData.yxq = getStringFromJson(lg, "yxq");
                lgData.gzmcwxx = getStringFromJson(lg, "gzmcwxx");
                if ((lgData.fbh.length() == 0) && (lgData.uid.length() == 17))
                    lgData.fbh = lgData.uid.substring(0, 13);
                if ((lgData.fbh.length() == 0) && (lgData.uid.length() == 13))
                    lgData.fbh = FileFunc.UidToFbh(lgData.uid);
                if (!lgData.gzmcwxx.equals("3"))
                    lgDatas.add(lgData);
            }

            JSONObject zbqys = fullJSONObject.getJSONObject("zbqys");
            JSONArray zbqyArrary = zbqys.getJSONArray("zbqy");
            JSONObject zb;
            Zbqy zbqy;
            for (i = 0; i < zbqyArrary.length(); i++) {
                zb = zbqyArrary.getJSONObject(i);
                zbqy = new Zbqy();
                zbqy.zbqyjd = getStringFromJson(zb, "zbqyjd");
                zbqy.zbqywd = getStringFromJson(zb, "zbqywd");
                zbqy.zbqybj = getStringFromJson(zb, "zbqybj");
                zbqy.zbqssj = getStringFromJson(zb, "zbqssj");
                zbqy.zbqzsj = getStringFromJson(zb, "zbjzsj");
                zbqy.zbqymc = getStringFromJson(zb, "zbqymc");
                zbqyList.add(zbqy);
            }
            try {
                JSONObject jbqys = fullJSONObject.getJSONObject("jbqys");
                JSONArray jbqyArrary = jbqys.getJSONArray("jbqy");
                JSONObject jb;
                Jbqy jbqy;
                for (i = 0; i < jbqyArrary.length(); i++) {
                    jb = jbqyArrary.getJSONObject(i);
                    jbqy = new Jbqy();
                    jbqy.jbqyjd = getStringFromJson(jb, "jbqyjd");
                    jbqy.jbqywd = getStringFromJson(jb, "jbqywd");
                    jbqy.jbqybj = getStringFromJson(jb, "jbqybj");
                    jbqy.jbqssj = getStringFromJson(jb, "jbqssj");
                    jbqy.jbqzsj = getStringFromJson(jb, "jbjzsj");
                    jbqyList.add(jbqy);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            dataErr = true;
        }
    }

    public static String getStringFromJson(JSONObject jsonObject, String str) {
        String s = "";
        try {
            s = jsonObject.getString(str);
        } catch (Exception e) {
            e.printStackTrace();
            return s;
        }
        return s;
    }


}
