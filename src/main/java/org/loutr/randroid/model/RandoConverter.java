package org.loutr.randroid.model;

import android.util.Log;
import android.util.Xml;
import com.google.android.gms.maps.model.LatLng;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by romain on 12/8/13.
 */
class RandoConverter implements Converter {
    @Override
    public Object fromBody(TypedInput typedInput, Type type) throws ConversionException {
        try{
            return parse(typedInput.in());
        }catch (Exception e){
            Log.d("error while converting R&R KML",e.getLocalizedMessage());
            throw new ConversionException(e);
        }
    }

    @Override
    public TypedOutput toBody(Object o) {
        //We don't send anything to the server
        return null;
    }


    private static final String ns = null;

	private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd",
			Locale.getDefault());

	// We don't use namespaces

	private Rando parse(InputStream in) throws XmlPullParserException,
			IOException, ParseException {
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			return readKml(parser);
		} finally {
			in.close();
		}
	}

	/**
	 * Reads the KML file. It should have a name element ending with the date in
	 * the yyyy-MM-dd format, and two Placemark elements each containing
	 * Description and coordinates elements.
	 *
	 * @param parser
	 * @return
	 * @throws org.xmlpull.v1.XmlPullParserException
	 * @throws java.io.IOException
	 * @throws java.text.ParseException
	 */
	private Rando readKml(XmlPullParser parser) throws XmlPullParserException,
            IOException, ParseException {
		Rando rando = new Rando();

		boolean isAller = true;
		List<LatLng> currentSegment = null;

		parser.require(XmlPullParser.START_TAG, ns, "kml");
		parser.nextTag();
		parser.require(XmlPullParser.START_TAG, ns, "Document");

		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();

            Log.d(this.getClass().getName(), "tag name : " + name);
			if (name.equals("name")) {
				// Get date from name
				rando.setDate(readName(parser));
			} else if (name.equals("Placemark")) {
				// Process the segment
				while (parser.next() != XmlPullParser.END_TAG) {
					if (parser.getEventType() != XmlPullParser.START_TAG) {
						continue;
					}

					String subName = parser.getName();
					Log.d(this.getClass().getName(), "subtag name : " + subName);
					if (subName.equals("description")) {
                        isAller = "Segment 1".equals(readText(parser));
					} else if (subName.equals("LineString")) {

						currentSegment = readLineString(parser);
					} else {
						skip(parser);
					}
				}

				if (isAller) {
					rando.setAller(currentSegment);
				} else {
					rando.setRetour(currentSegment);
				}

			} else {
				skip(parser);
			}
		}
		return rando;
	}

	private List<LatLng> readLineString(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		List<LatLng> segment = null;
		parser.require(XmlPullParser.START_TAG, ns, "LineString");

		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}

			String name = parser.getName();
			Log.d(this.getClass().getName(), "line string tag name : " + name);
			if (name.equals("coordinates")) {
				segment = readCoords(parser);
			} else {
				skip(parser);
			}
		}

		return segment;
	}

	private Calendar readName(XmlPullParser parser) throws IOException,
			XmlPullParserException, ParseException {
		parser.require(XmlPullParser.START_TAG, ns, "name");
		String name = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "name");

		Calendar date = Calendar.getInstance();
		date.setTime(df.parse(name.substring(name.length() - 10)));

		return date;
	}

	private List<LatLng> readCoords(XmlPullParser parser) throws IOException,
			XmlPullParserException {

		List<LatLng> segment = new ArrayList<LatLng>();
		String coords = readText(parser);
		if (coords.length() > 0) {
			Log.d(this.getClass().toString(), "coords = " + coords);

			for (String coord : coords.split(System
					.getProperty("line.separator"))) {

				String[] tmp = coord.split(",");
				segment.add(new LatLng(Double.parseDouble(tmp[1]), Double
						.parseDouble(tmp[0])));
			}
		}
		return segment;
	}

	// For the tags title and summary, extracts their text values.
	private String readText(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		String result = "";
		if (parser.next() == XmlPullParser.TEXT) {
			result = parser.getText();
			parser.nextTag();
		}
		return result.trim();
	}

	// Skips tags the parser isn't interested in. Uses depth to handle nested
	// tags. i.e.,
	// if the next tag after a START_TAG isn't a matching END_TAG, it keeps
	// going until it
	// finds the matching END_TAG (as indicated by the value of "depth" being
	// 0).
	private void skip(XmlPullParser parser) throws XmlPullParserException,
			IOException {
		if (parser.getEventType() != XmlPullParser.START_TAG) {
			throw new IllegalStateException();
		}
		int depth = 1;
		while (depth != 0) {
			switch (parser.next()) {
			case XmlPullParser.END_TAG:
				depth--;
				break;
			case XmlPullParser.START_TAG:
				depth++;
				break;
			}
		}
	}

}
