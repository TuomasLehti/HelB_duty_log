# HelB_duty_log
<p>Java classes for handling bus drivers' duty log files from Helsingin Bussiliikenne (abbreviated commonly HelB).</p>

<p>Application stores the duty lists as a three-layer data structure.</p>

<ul>
  <li>HelBDutyItem stores a single item of a drivers' duty.</li>
  <li>HelBDuty stores a single duty, in other words the work for a single day.</li>
  <li>HelBDutyList stores a list of duties.</li>
</ul>
 
<p>Reader and writer classes handle the input and output between memory and files.</p>

<ul>
  <li>HelBDutyListReader is an absract class for all readers.</li>
  <li>HelBDutyListWriter is an absract class for all writers.</li>
</ul>
  
<p>Duty log files are sent to drivers via email as pdf-files. They need to be converted to xml-format. It would be possible to do the conversion with existing software, but that would require extra effort from the user. I haven't found existing Java packages, which would extract the text from a pdf-file, so I have made my own.</p>

<ul>
  <li>PDFByteArray handles PDF files in the lowest possible level, byte level.</li>
  <li>PDFObject handles a single PDF object.</li>
  <li>PDFObjectList handles the file structure of the PDF document. All objects and the trailer of a PDF file can be accessed through this class.</li>
  <li>PDFTextExtractor is a quick and dirty class to extract text from a pdf. It is coded with HelB duty log files in mind, and takes a lot of shortcuts. Maybe some day it will be a clean and well-behaved reader conforming to all standards, but in it's current state it works, so further developement will be on hold.</li>
  <li>PDFTextItem is a small class, which holds a single text object from PDF-files object stream. PDFTextExtractor handles lists of PDFTextItems.
</ul>

<h2>Changes in march 2019</h2>

<p>In march 2019 HelB changed the format of it's duty log files. Printed out the documents look and feel the same as before, but the program wasn't able to read them properly anymore.</p>

<p>Upon investigating of the document structure it was found out, that every text snippet is surrounded by a bounding or clipping box, which is called surrounding box in this library. The y-coordinates of these boxes were the same on text items, which were meant to be on the same line. The same could not be said for the y-coordinates of the text snippets.</p>

<p>Previously it was still quite easy to work around the text items being in different y-coordinates. The prospect of having to change the workaround every time HelB changes something has motivated me to search for a more stable solution. I hope the solution with the concept of a surrounding box will prove to be stable.</p>
