package Topic.JAVA.plotsum;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.io.FileReader;
import java.io.FileWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.aip.nlp.AipNlp;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import OCR.JAVA.Film;

/* 
 * Library:
 * Depending on Baidu API, see https://ai.baidu.com/ai-doc/NLP/Tlb3dlhoo
*/
public class GetEntityNames {
	//设置APPID/AK/SK
	static String APP_ID = "App ID";
	static String API_KEY = "Api Key";
	static String SECRET_KEY = "Secret Key";
	private static final String TAR = "Topic/JAVA/plot_entities(src).txt";
	private static String token;

	static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().build();

	//For this class and JSON conversion
	public EntityEnquiryQueryList[] content_list;

	public GetEntityNames(String input) {
		this.content_list = new EntityEnquiryQueryList[1];
		content_list[0] = new EntityEnquiryQueryList(input);
	}
	
	public static void main(String[] args) throws IOException {
		ArrayList<Film> films = Film.initAllFilms();
		int st = 300, ed = films.size();
		int i = st;
		for (i=st; i<ed; i++) {
			EntityResult r;
			try {
				r = extract(films.get(i));
			}catch(Exception e) {
				r = new EntityResult(PlotEntityResult.getErrorDefaultArray(e.getMessage()), PlotEntityResult.getErrorDefaultArray(e.getMessage()), films.get(i).key);
			}
			File file = new File(TAR);
			BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
			writer.append(r.toString());
			writer.close();
			System.out.println((i-st+1) + "/" + (ed-st));
		}
	}

	protected static EntityResult extract(Film film) throws JSONException, IOException {
		String plot = film.plot;
		plot = plot.replaceAll("\\{LINE_CUT\\}", "\n");
		plot = plot.replaceAll("\\{QUOTE\\}", "“");
		plot = plot.replaceAll("\\{COMMA\\}", "，");

		JSONObject json = getPlotLocationTime(plot);
		try{
			JSONObject output = json.getJSONArray("results_list").getJSONObject(0);
			JSONArray res = output.getJSONArray("results");
			JSONArray locs = res.getJSONObject(0).getJSONArray("items");
			JSONArray times = res.getJSONObject(1).getJSONArray("items");
			//Iterate through locations and convert to PlotEntityResult
			ArrayList<PlotEntityResult> locsRes = new ArrayList<PlotEntityResult>();
			for(int i = 0; i < locs.length(); ++i) {
				JSONObject loc = locs.getJSONObject(i);
				String locStr = loc.getString("text");
				double conf = (loc.getDouble("prob"));
				PlotEntityResult locRes = new PlotEntityResult(locStr, conf);
				locsRes.add(locRes);
			}
			//Iterate through times and convert to PlotEntityResult
			ArrayList<PlotEntityResult> timesRes = new ArrayList<PlotEntityResult>();
			for(int i = 0; i < times.length(); ++i) {
				JSONObject time = times.getJSONObject(i);
				String timeStr = time.getString("text");
				double conf = (time.getDouble("prob"));
				PlotEntityResult timeRes = new PlotEntityResult(timeStr, conf);
				timesRes.add(timeRes);
			}
			return new EntityResult(locsRes, timesRes, film.key);
		}catch(Exception e) {
			String err = ("ERROR: " + json.getString("error_msg") + " : " + json.getInt("error_code"));
			return new EntityResult(PlotEntityResult.getErrorDefaultArray(err), PlotEntityResult.getErrorDefaultArray(err), film.key);
		}
	}

	@SuppressWarnings("deprecation")
	protected static JSONObject getPlotLocationTime(String text) throws JSONException, IOException {
		String input = text.replaceAll("\n", " ");
		input = input.replaceAll("\"", "“");
		//System.out.println(input);
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(new GetEntityNames(input));
		//System.out.println(json);

		MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, json);
		
		token = getAccessToken();
        Request request = new Request.Builder()
            .url("https://aip.baidubce.com/rpc/2.0/nlp/v1/txt_monet?charset=UTF-8&access_token=" + token)
            .method("POST", body)
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        String bodyOut = (response.body().string());
		response.close();
		return new JSONObject(bodyOut);
	}

	@SuppressWarnings("unused")
	@Deprecated
	private static AipNlp startBaiduAPI() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File("Topic/JAVA/plotsum/baidu_key.txt")));
		APP_ID = reader.readLine();
		API_KEY = reader.readLine();
		SECRET_KEY = reader.readLine();
		reader.close();

		// 初始化一个AipNlp
		AipNlp client = new AipNlp(APP_ID, API_KEY, SECRET_KEY);

		// 可选：设置网络连接参数
		client.setConnectionTimeoutInMillis(2000);
		client.setSocketTimeoutInMillis(60000);
		return client;
	}

	/**
     * 从用户的AK，SK生成鉴权签名（Access Token）
     *
     * @return 鉴权签名（Access Token）
     * @throws IOException IO异常
     */
	@SuppressWarnings("deprecation")
    static String getAccessToken() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File("Topic/JAVA/plotsum/baidu_key.txt")));
		APP_ID = reader.readLine();
		API_KEY = reader.readLine();
		SECRET_KEY = reader.readLine();
		reader.close();

        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials&client_id=" + API_KEY
                + "&client_secret=" + SECRET_KEY);
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/oauth/2.0/token")
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        return new JSONObject(response.body().string()).getString("access_token");
    }
    
}


class PlotEntityResult {
	public String text;
	public double conf;

	public PlotEntityResult(String text, double conf) {
		this.text = text;
		this.conf = conf;
	}

	public static ArrayList<PlotEntityResult> getErrorDefaultArray(String msg) {
		ArrayList<PlotEntityResult> res = new ArrayList<PlotEntityResult>();
		res.add(new PlotEntityResult(msg, 0.0));
		return res;
	}
}

class EntityEnquiryQueryListChild {
	final static EntityEnquiryQueryListChild LOC = new EntityEnquiryQueryListChild("地点");
	final static EntityEnquiryQueryListChild ROLE = new EntityEnquiryQueryListChild("角色");

	public String query;

	public EntityEnquiryQueryListChild(String query) {
		this.query = query;
	}
}

class EntityEnquiryQueryList {
	public String content;
	public ArrayList<EntityEnquiryQueryListChild> query_list;

	public EntityEnquiryQueryList(String content) {
		this.content = content;
		this.query_list = new ArrayList<EntityEnquiryQueryListChild>();
		this.query_list.add(EntityEnquiryQueryListChild.LOC);
		this.query_list.add(EntityEnquiryQueryListChild.ROLE);
	}
}

class EntityResult {
	public ArrayList<PlotEntityResult> locs;
	public ArrayList<PlotEntityResult> times;
	public String key;

	public EntityResult(ArrayList<PlotEntityResult> locs, ArrayList<PlotEntityResult> times, String filmKey) {
		this.locs = locs;
		this.times = times;
		this.key = filmKey;
	}

	@Override
	public String toString() {
		String base = "";
		String locStr = "";
		for (PlotEntityResult plotEntityResult : locs) {
			locStr += plotEntityResult.text + "\n";
		}
		if(locStr.contains("ERROR")) locStr = "";
		//base += locStr;
		String timeStr = "";
		for (PlotEntityResult plotEntityResult : times) {
			timeStr += plotEntityResult.text + "\n";
		}
		if(timeStr.contains("ERROR")) timeStr = "";
		base += timeStr;
		return base;
	}
}