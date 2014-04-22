package servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URISyntaxException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import filePath.FilePath;

import Relative2absolute.Rel2abs;

import myComposer.Composer;

/**
 * Servlet implementation class theComposerServlet
 */
@WebServlet(
        name = "ComposerServlet", 
        urlPatterns = {"/theComposerServlet"}
    )
public class theComposerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public theComposerServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/*System.out.println(request.getParameter("lrc"));
		response.setContentType("application/json;charset=utf-8");
		Composer com1 = new Composer("Hello Today is a good day");
		String jsonResult = com1.getJSONOutput();
		JSONArray ja = new JSONArray(jsonResult);
		JSONObject jo = new JSONObject();
		jo.put("note", ja);
		PrintWriter pw = response.getWriter(); 
        //pw.print(json.toString());
        pw.print(jo.toString());
        pw.close();*/
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/*Reader reader = request.getReader();
		char[] a = new char[500];
		reader.read(a);
		System.out.println(a);
		*/
		//System.out.println(request.getParameter("lrc"));
		
		FilePath.allPatternOne = this.getClass().getClassLoader().getResource("src/main/resources/allPatternOne.txt").getPath();
		FilePath.allPatternTwo = this.getClass().getClassLoader().getResource("src/main/resources/allPatternTwo.txt").getPath();
		String dicPath = this.getClass().getClassLoader().getResource("src/main/resources/Dictionary.lg").getPath();
		dicPath = dicPath.substring(0,dicPath.length()-3);
		FilePath.dictionary = dicPath;
		FilePath.generatedMIDI = this.getClass().getClassLoader().getResource("src/main/resources/generatedFile.mid").getPath();
		response.setContentType("application/json;charset=utf-8");
		String inputLrc = request.getParameter("lrc");
		int beats = Integer.parseInt(request.getParameter("beats"));
		int beatType = Integer.parseInt(request.getParameter("beatType"));
		int tone = Integer.parseInt(request.getParameter("major"));
		JSONArray notes = Rel2abs.getJsonOutput(inputLrc, beatType, beats,tone);
		//Composer com1 = new Composer(inputLrc,4,4);
		//String jsonResult = com1.getJSONOutput();
		//JSONArray ja = new JSONArray(jsonResult);
		JSONObject jo = new JSONObject();
		jo.put("notes", notes);
		PrintWriter pw = response.getWriter(); 
        //pw.print(json.toString());
        pw.print(jo.toString());
        pw.close();
	}

}
