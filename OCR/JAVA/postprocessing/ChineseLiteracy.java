package OCR.JAVA.postprocessing;

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

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import OCR.JAVA.Film;

/* 
 * Library:
 * Depending on Baidu API, see https://cloud.baidu.com/doc/NLP/s/Nk6z52ci5
*/
public class ChineseLiteracy {
	//设置APPID/AK/SK
	public static String APP_ID = "App ID";
	public static String API_KEY = "Api Key";
	public static String SECRET_KEY = "Secret Key";
	private static final String TAR = "OCR/literacy.csv";
	private static String token;
	private static JSONObject TEMP_ITEM = null;

	static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().build();

	private static class checkResult {
		public String corrected, original;
		public String filmKey;
		public int errorNumber;
		public ArrayList<String> fixes;

		public checkResult(String corrected, String original, String filmKey, int errorNumber) {
			this.corrected = corrected;
			this.original = original;
			this.filmKey = filmKey;
			this.errorNumber = errorNumber;
			this.fixes = new ArrayList<String>();
		}

		public String toString() {
			for (int i=0; i<fixes.size(); i++) {
				if(fixes.get(i).contains("标点")) {
					fixes.remove(i);
					i--;
					errorNumber--;
				}
			}
			String base = filmKey + "," + corrected.replaceAll(",", "，").replaceAll("\"", "“").replaceAll("\n", "{LINE_CUT}") + "," + original.replaceAll(",", "，").replaceAll("\"", "“").replaceAll("\n", "{LINE_CUT}") + "," + errorNumber;
			for (String string : fixes) {
				base = base.concat("," + string.replaceAll(",", "，").replaceAll("\"", "“").replaceAll("\n", "{LINE_CUT}"));
			}
			return base;
		}
	}

	
	public static void main(String[] args) throws IOException {
		ArrayList<Film> films = Film.initAllFilms();

		//DEBUG
		/*ArrayList<Film> films = new ArrayList<Film>();
		films.add(new Film("Kanashii Debug Monogatari", "悲しくなるデバッグ物語", 1999,
			"A Sad Story of Debugging", "Northeast Film Studio", "Colour", 12,
			"", "源法子", "源法子",
			"青井学 / 井上富", "源法子 (背景设计)",
			"A sad story about a \"PROGRAMMAR\" trying to write a program."));
		films.add(new Film("Tsubaki Sanjuurou", "椿三十郎", 1999,
			"Tsubaki Sanjuro", "Northeast Film Studio", "Colour", 12,
			"", "黑澤明", "黑澤明",
			"三船", "",
			"Toshiro Mifune swaggers and snarls to brilliant comic effect in Kurosawa's tightly paced, beautifully composed \"Sanjuro.\" In this companion piece and sequel to \"Yojimbo,\" jaded samurai Sanjuro helps an idealistic group of young warriors weed out their clan's evil influences, and in the process turns their image of a proper samurai on its ear."));
		films.add(new Film("hokuto no ken", "北斗之拳", 1988,
			"Fist of the North Star", "Northeast Film Studio", "Colour", 12,
			"", "原哲夫", "武论尊",
			"拳四郎", "",
			"人类文明毁于核子战争的未来，存活下来的人类过着弱肉强食的生活，直到出现了一个胸口带着北斗七星状伤痕、古老中国神秘暗杀拳法\"北斗神拳\"的传人————拳四郎成为救世主。"));*/
		
		int st = 350, ed = films.size();
		int i = st;
		for (i=st; i<ed; i++) {
			checkResult r;
			try {
				r = checkFilmPlotLiteracy(films.get(i));
				if(TEMP_ITEM != null)
					r.fixes = fixList(TEMP_ITEM);
				TEMP_ITEM = null;
			}catch(Exception e) {
				r = new checkResult("ERROR", "ERROR", films.get(i).key, -1);
			}
			File file = new File(TAR);
			BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
			writer.append(r.toString());
			writer.newLine();
			writer.close();
			//if(i>6) break; // for test
			System.out.println((i-st+1) + "/" + (ed-st));
		}
	}

	private static ArrayList<String> fixList(JSONObject itemOutput) {
		JSONArray arr = itemOutput.getJSONArray("details");
		ArrayList<String> output = new ArrayList<String>();
		for (int i = 0; i < arr.length(); ++i) {
			JSONObject rec = arr.getJSONObject(i);
			String sentence = rec.getString("sentence_fixed");
			try {
				JSONArray fixes = rec.getJSONArray("vec_fragment");
				for(int j = 0; j < fixes.length(); ++j) {
					JSONObject fix = fixes.getJSONObject(j);
					String fixStr = fix.getString("explain_long");
					String fixType = fix.getString("explain");
					output.add(sentence+"："+fixStr+"。"+fixType);
				}
			}catch(Exception e) {
				
			}
		}
		return output;
	}

	protected static checkResult checkFilmPlotLiteracy(Film film) throws JSONException, IOException {
		String plot = film.plot;
		plot = plot.replaceAll("\\{LINE_CUT\\}", "\n");
		plot = plot.replaceAll("\\{QUOTE\\}", "“");
		plot = plot.replaceAll("\\{COMMA\\}", "，");
		String key = film.key;

		JSONObject json = checkChineseLiteracy(plot);
		try{
			JSONObject output = json.getJSONObject("item");
			TEMP_ITEM = output;
			String cor = (output.getString("correct_query"));
			String ori = (output.getString("text"));
			int error = (output.getInt("error_num"));
			return new checkResult(cor, ori, key, error);
		}catch(Exception e) {
			String err = ("ERROR: " + json.getString("error_msg") + " : " + json.getInt("error_code"));
			return new checkResult(err, plot, key, -1);
		}
	}

	@SuppressWarnings("deprecation")
	protected static JSONObject checkChineseLiteracy(String text) throws JSONException, IOException {
		String input = text.replaceAll("\n", "\\\\n");
		input = input.replaceAll("\"", "“");
		System.out.println(input);
		String json = "{\"text\":\"" + input + "\"}";
		//System.out.println(json);

		MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, json);
		
		token = getAccessToken();
        Request request = new Request.Builder()
            .url("https://aip.baidubce.com/rpc/2.0/nlp/v2/text_correction?charset=UTF-8&access_token=" + token)
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
		BufferedReader reader = new BufferedReader(new FileReader(new File("OCR/JAVA/postprocessing/baidu_key.txt")));
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
		BufferedReader reader = new BufferedReader(new FileReader(new File("OCR/JAVA/postprocessing/baidu_key.txt")));
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
