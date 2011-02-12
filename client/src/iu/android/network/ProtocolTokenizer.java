package iu.android.network;

/*************************************************************************************************************
 * IU 1.0b, a java real time strategy game 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 ************************************************************************************************************/

import java.io.Reader;
import java.io.StreamTokenizer;


/**
 * This class uses a StreamTokenizer to help reading data transfered using our <code>Protocol</code>  
 */
public class ProtocolTokenizer
{
	//
	// The stream tokenizer we use for our protocol.
	//
	private StreamTokenizer st = null;

	//
	// We use this member to read data from the underlying stream
	//
	private Reader streamReader;

	
	/**
	 * Constructor
	 */
	public ProtocolTokenizer (Reader streamReader)
	{
		//
		// Connect the stream reader
		//
		this.streamReader = streamReader;
		
		//
		// Build a new tokenizer to handle this stream
		//
		this.st = new StreamTokenizer (this.streamReader);
	   
		//
		// We delete any syntax before starting building ours
		//
	   this.st.resetSyntax();

	   /*
	    * FIXME: Complete this implementation! 
	    *
	   // set the syntax to read -9.0123E+5 as a single token
	   st.whitespaceChars((int)'\u0000',(int)'\u0020');
	   st.wordChars((int)'.',(int)'.');
	   st.wordChars((int)'-',(int)'-');
	   st.wordChars((int)'+',(int)'+');
	   st.wordChars((int)'0',(int)'9');
	   st.wordChars((int)'E',(int)'E');

	   int stn = st.nextToken(); zliberror.assert(stn == StreamTokenizer.TT_WORD);
	   stn = st.nextToken();
	   while(stn != StreamTokenizer.TT_EOF)
	   {
	       String tag = st.sval;
		stn = st.nextToken();
		
		float val = (float)Double.parseDouble(st.sval);
		stn = st.nextToken();
		...
	   }

	   br.close();*/
	}
}
