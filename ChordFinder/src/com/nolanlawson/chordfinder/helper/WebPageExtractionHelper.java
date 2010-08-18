package com.nolanlawson.chordfinder.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.Html;
import android.text.TextUtils;

import com.nolanlawson.chordfinder.util.UtilLogger;

public class WebPageExtractionHelper {

	private static UtilLogger log = new UtilLogger(WebPageExtractionHelper.class);
	
	public static enum ChordWebpage {
		UltimateGuitar;
	}
	
    // html tag or html escaped character
    private static Pattern htmlObjectPattern = Pattern.compile(
            "(" +
            "<\\s*style.*?>.*?<\\s*/style\\s*>" + // style span
            "|" + // OR
            "<\\s*script.*?>.*?<\\s*/script\\s*>" + // script span
            "|" + // OR
            "<\\s*head.*?>.*?<\\s*/head\\s*>" + // head span
            "|" + // OR
            "<[^>]++>" + // html tag, such as '<br/>' or '<a href="www.google.com">'
            "|" + // OR
            "&[^; \n\t]++;" + // escaped html character, such as '&amp;' or '&#0233;'
            ")", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);


    // HTML newline tag, such as '<p>' or '<br/>'
    private static Pattern htmlNewlinePattern = Pattern.compile(
            "<(?:p|br)\\s*+(?:/\\s*+)?>", Pattern.CASE_INSENSITIVE);
	
	private static Pattern ultimateGuitarPattern = Pattern.compile("<pre>(.*?)</pre>", 
				Pattern.DOTALL|Pattern.CASE_INSENSITIVE);
	
	public static String extractChordChart(ChordWebpage webpage, String html) {
		
		Pattern pattern = null;
		
		switch (webpage) {
		case UltimateGuitar:
			pattern = ultimateGuitarPattern;
		}
		
		Matcher matcher = pattern.matcher(html);
		
		if (!matcher.find()) {
			return null;
		}
		
		String chordChartHtml = matcher.group(1);
		
		return convertHtmlToText(chordChartHtml);
		
	}
	
	private static String convertHtmlToText(String htmlText) {

        StringBuilder plainText = new StringBuilder();

        // replace HTML tags with spaces and unescape HTML characters
        Matcher matcher = htmlObjectPattern.matcher(htmlText);
        int searchIndex = 0;
        while (matcher.find(searchIndex)) {
            int start = matcher.start();
            int end = matcher.end();
            String htmlObject = matcher.group();

            String replacementString;
            if (htmlText.charAt(start) == '&') { // html escaped character
                
                if (htmlObject.equalsIgnoreCase("&nbsp;")) {
                    replacementString = " "; // apache replaces nbsp with unicode \xc2\xa0, but we prefer just " "
                } else {
                    replacementString = Html.fromHtml(htmlObject).toString();
                }

                if (TextUtils.isEmpty(replacementString)) { // ensure non-empty - otherwise offsets would be screwed up
                    log.i("Warning: couldn't escape html string: '" + htmlObject + "'");
                    replacementString = " ";
                }
            } else if (htmlNewlinePattern.matcher(htmlObject).matches()) { // newline tag
                if (htmlObject.toLowerCase().contains("p")) { // paragraph break
                    replacementString = "\n\n";
                } else { // 'br' (carriage return)
                    replacementString = "\n";
                }
            } else { // html tag or <style>/<script> span
                replacementString = " ";
            }

            plainText.append(htmlText.substring(searchIndex, start));

            plainText.append(replacementString);

            searchIndex = end;

        }
        
        plainText.append(htmlText.substring(searchIndex, htmlText.length()));

        return plainText.toString();
    }
	
}
