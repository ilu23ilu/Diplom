import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class BooleanSearchEngine implements SearchEngine {

    private Map<String, List<PageEntry>> searchResults = new HashMap<>();

    public BooleanSearchEngine(File pdfsDir) throws IOException {
        var stopList = new StopList();
        File[] listOfPdfs = pdfsDir.listFiles();
        if (listOfPdfs != null) {
            for (var pdfFile : listOfPdfs) {
                var doc = new PdfDocument(new PdfReader(pdfFile));
                for (int page = 1; page <= doc.getNumberOfPages(); page++) {
                    PdfPage currentPage = doc.getPage(page);
                    var text = PdfTextExtractor.getTextFromPage(currentPage);
                    var words = text.split("\\P{IsAlphabetic}+");
                    Map<String, Integer> freqs = new HashMap<>();
                    for (var word : words) {
                        if (word.isEmpty()) {
                            continue;
                        } else if (stopList.getStopList().contains(word.toLowerCase())) {
                            continue;
                        }
                        word = word.toLowerCase();
                        freqs.put(word, freqs.getOrDefault(word, 0) + 1);
                    }
                    for (var word : freqs.keySet()) {
                        List<PageEntry> pageEntries = new ArrayList<>();
                        if (!searchResults.containsKey(word)) {
                            pageEntries.add(new PageEntry(pdfFile.getName(), page, freqs.get(word)));
                            searchResults.put(word, pageEntries);
                        } else {
                            List<PageEntry> value = searchResults.get(word); // word, <devops.pdf, 1, 3> + <devops.pdf, 2, 2>
                            value.add(new PageEntry(pdfFile.getName(), page, freqs.get(word)));
                            value.sort(PageEntry::compareTo);
                            searchResults.put(word, value);
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<PageEntry> search(String word) {
        var lowerCaseWord = word.toLowerCase();
        var split = lowerCaseWord.split("\\P{IsAlphabetic}+");
        if (split.length == 1) {
            if (searchResults.containsKey(lowerCaseWord)) {
                return searchResults.get(lowerCaseWord);
            }
        }
        return searchMultiText(split);
    }
    private List<PageEntry> searchMultiText(String[] split) {
        List<PageEntry> resultList = new ArrayList<>();
        Set<String> uniqueWords = Arrays.stream(split).collect(Collectors.toSet());
        for (int i = 0; i < uniqueWords.size(); i++) {
            if (searchResults.containsKey(split[i])) {
                resultList.addAll(searchResults.get(split[i]));
            }
        }
        for (int i = 0; i < resultList.size(); i++) {
            for (int j = i + 1; j < resultList.size(); j++) {
                if ((resultList.get(i).getPdfName().equals(resultList.get(j).getPdfName())) &&
                        (resultList.get(i).getPage() == (resultList.get(j).getPage()))) {
                    PageEntry entry = new PageEntry(resultList.get(i).getPdfName(), resultList.get(i).getPage(),
                            (resultList.get(i).getCount() + resultList.get(j).getCount()));
                    resultList.remove(j);
                    resultList.remove(i);
                    resultList.add(i, entry);
                    j--;
                }
            }
        }
        resultList.sort(PageEntry::compareTo);
        return resultList;
    }
}