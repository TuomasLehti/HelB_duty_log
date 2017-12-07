# HelB_duty_log
Java classes for handling bus drivers' duty log files from Helsingin Bussiliikenne (abbreviated commonly HelB).

<ul>
  <li>PDFByteArray handles PDF files in the lowest possible level, byte level.</li>
  <li>PDFObject handles a single PDF object.</li>
  <li>PDFObjectList handles the file structure of the PDF document. All objects and the trailer of a PDF file can be accessed through this class.</li>
  <li>PDFTextExtractor is a quick and dirty class to extract text from a pdf. It is coded with HelB duty log files in mind, and takes a lot of shortcuts. Maybe some day it will be a clean and well-behaved reader conforming to all standards, but in it's current state it works, so further developement will be on hold.</li>
  <li>PDFTextItem is a small class, which holds a single text object from PDF-files object stream. PDFTextExtractor handles lists of PDFTextItems.
</ul>
