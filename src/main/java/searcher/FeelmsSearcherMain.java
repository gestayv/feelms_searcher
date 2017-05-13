/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package searcher;

import db.Film;
import db.MySqlConn;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.TermQuery;
/**
 *
 * @author ichigo
 */
public class FeelmsSearcherMain 
{
    //  Ruta en la que se almacena el índice
    private static final String index = "./src/main/resources/index";
    //  String query, esto es input del usuario.
    private static final String query = "wars star";
    
    
    public static void main(String[] args) throws IOException, SQLException
    {
        Path indexDir = Paths.get(index);
        FeelmsSearcherMain fsm = new FeelmsSearcherMain();
        int num = fsm.index(indexDir);
        System.out.println(num);
        fsm.searchIndex(indexDir, query, 10);
    }
    
    //  searchIndex, se encarga de buscar un string en el índice
    //  - indexDir: ruta del índice
    //  - queryStr: string que se busca en el indice.
    //  - maxHits: número máximo de resultados que se devuelven.
    private void searchIndex(Path indexDir, String queryStr, int maxHits) throws IOException
    {
        queryStr = queryStr.toLowerCase();
        
        Directory directory = FSDirectory.open(indexDir);
		
        IndexReader indexReader  = DirectoryReader.open(directory);

        IndexSearcher searcher = new IndexSearcher(indexReader);

        Analyzer analyzer = new StandardAnalyzer();
        
        BooleanQuery.Builder qBuilder =  new BooleanQuery.Builder();
        
        String[] input = queryStr.split(" ");
        
        if(input.length == 1)
        {
            Query qr2 = new TermQuery(new Term("original_title", queryStr));
            Query qr1 = new TermQuery(new Term("title", queryStr));

            qBuilder.add(new BooleanClause(qr1, BooleanClause.Occur.SHOULD));
            qBuilder.add(new BooleanClause(qr2, BooleanClause.Occur.SHOULD));
        }
        else if(input.length > 1)
        {
            MultiPhraseQuery.Builder mpq1 = new MultiPhraseQuery.Builder();
            MultiPhraseQuery.Builder mpq2 = new MultiPhraseQuery.Builder();
            for (String string : input) {
                mpq1.add(new Term("original_title", string));
                mpq2.add(new Term("title", string));
            }
            MultiPhraseQuery mpquery1 = mpq1.build();
            MultiPhraseQuery mpquery2 = mpq2.build();
            qBuilder.add(new BooleanClause(mpquery1, BooleanClause.Occur.SHOULD));
            qBuilder.add(new BooleanClause(mpquery2, BooleanClause.Occur.SHOULD));
        }
        
        BooleanQuery queryNombres = qBuilder.build();
        
        TopDocs topDocs = searcher.search(queryNombres, maxHits);
        
        ScoreDoc[] hits = topDocs.scoreDocs;
          
         for (int i = 0; i < hits.length; i++) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            System.out.println(d.get("original_title") + " o "+ d.get("title") +" Score :"+hits[i].score);
        }
    }
    
    //  index: construye el índice con las películas en la bd sql
    //  - indexDir: ruta en la que se guardará el índice en disco.
    private int index(Path indexDir) throws IOException, SQLException
    {
        
        int numIndex = 0;
        
        MySqlConn conn = new MySqlConn("root", "root", "localhost", "3306", "feelms_sql");
        if(conn.test())
        {
            StandardAnalyzer stda = new StandardAnalyzer();
            //  revisar como se comporta el index, crearlo de nuevo siempre
            //  o utilizar append. Por ahora se creará de nuevo siempre.
            IndexWriterConfig indexconfig = new IndexWriterConfig(stda);
            indexconfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            
            IndexWriter indexw = new IndexWriter(FSDirectory.open(indexDir), indexconfig);
            
            List<Film> movies = conn.getFilms();
            
            for (Film m : movies) {
                Document doc = new Document();
                
                doc.add(new TextField("original_title", m.getNombreOriginal(), Field.Store.YES));
                doc.add(new TextField("title", m.getNombre(), Field.Store.YES));
                indexw.addDocument(doc);
            }
            
            numIndex = indexw.maxDoc();
            indexw.close();
        }
        return numIndex;
    }
    
}
