package org.jabref.logic.importer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.jabref.logic.bibtex.FieldContentFormatterPreferences;
import org.jabref.logic.importer.fetcher.AbstractIsbnFetcher;
import org.jabref.logic.importer.fetcher.GoogleScholar;
import org.jabref.logic.importer.fetcher.GrobidCitationFetcher;
import org.jabref.logic.importer.fetcher.JstorFetcher;
import org.jabref.logic.importer.fetcher.MrDLibFetcher;
import org.jabref.logic.importer.fetcher.isbntobibtex.DoiToBibtexConverterComIsbnFetcher;
import org.jabref.logic.importer.fetcher.isbntobibtex.EbookDeIsbnFetcher;
import org.jabref.logic.importer.fetcher.isbntobibtex.OpenLibraryIsbnFetcher;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.preferences.FilePreferences;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WebFetchersTest {

    private ImportFormatPreferences importFormatPreferences;
    private ImporterPreferences importerPreferences;
    private final ClassGraph classGraph = new ClassGraph().enableAllInfo().acceptPackages("org.jabref");

    @BeforeEach
    void setUp() {
        importFormatPreferences = mock(ImportFormatPreferences.class, Answers.RETURNS_DEEP_STUBS);
        importerPreferences = mock(ImporterPreferences.class);
        FieldContentFormatterPreferences fieldContentFormatterPreferences = mock(FieldContentFormatterPreferences.class);
        when(importFormatPreferences.getFieldContentFormatterPreferences()).thenReturn(fieldContentFormatterPreferences);
    }

    @Test
    void getIdBasedFetchersReturnsAllFetcherDerivingFromIdBasedFetcher() {
        Set<IdBasedFetcher> idFetchers = WebFetchers.getIdBasedFetchers(importFormatPreferences, importerPreferences);

        try (ScanResult scanResult = classGraph.scan()) {
            ClassInfoList controlClasses = scanResult.getClassesImplementing(IdBasedFetcher.class.getCanonicalName());
            Set<Class<?>> expected = new HashSet<>(controlClasses.loadClasses());

            expected.remove(AbstractIsbnFetcher.class);
            expected.remove(IdBasedParserFetcher.class);

            // Remove special ISBN fetcher since we don't want to expose them to the user
            expected.remove(OpenLibraryIsbnFetcher.class);
            expected.remove(EbookDeIsbnFetcher.class);
            expected.remove(DoiToBibtexConverterComIsbnFetcher.class);

            // Remove the following, because they don't work at the moment
            expected.remove(JstorFetcher.class);
            expected.remove(GoogleScholar.class);

            assertEquals(expected, getClasses(idFetchers));
        }
    }

    @Test
    void getEntryBasedFetchersReturnsAllFetcherDerivingFromEntryBasedFetcher() {
        Set<EntryBasedFetcher> idFetchers = WebFetchers.getEntryBasedFetchers(
                mock(ImporterPreferences.class),
                importFormatPreferences,
                mock(FilePreferences.class),
                mock(BibDatabaseContext.class));

        try (ScanResult scanResult = classGraph.scan()) {
            ClassInfoList controlClasses = scanResult.getClassesImplementing(EntryBasedFetcher.class.getCanonicalName());
            Set<Class<?>> expected = new HashSet<>(controlClasses.loadClasses());

            expected.remove(EntryBasedParserFetcher.class);
            expected.remove(MrDLibFetcher.class);
            assertEquals(expected, getClasses(idFetchers));
        }
    }

    @Test
    void getSearchBasedFetchersReturnsAllFetcherDerivingFromSearchBasedFetcher() {
        Set<SearchBasedFetcher> searchBasedFetchers = WebFetchers.getSearchBasedFetchers(importFormatPreferences, importerPreferences);
        try (ScanResult scanResult = classGraph.scan()) {
            ClassInfoList controlClasses = scanResult.getClassesImplementing(SearchBasedFetcher.class.getCanonicalName());
            Set<Class<?>> expected = new HashSet<>(controlClasses.loadClasses());

            // Remove interfaces
            expected.remove(SearchBasedParserFetcher.class);

            // Remove the following, because they don't work atm
            expected.remove(JstorFetcher.class);
            expected.remove(GoogleScholar.class);

            expected.remove(PagedSearchBasedParserFetcher.class);
            expected.remove(PagedSearchBasedFetcher.class);

            // Remove GROBID, because we don't want to show this to the user
            expected.remove(GrobidCitationFetcher.class);

            assertEquals(expected, getClasses(searchBasedFetchers));
        }
    }

    @Test
    void getFullTextFetchersReturnsAllFetcherDerivingFromFullTextFetcher() {
        Set<FulltextFetcher> fullTextFetchers = WebFetchers.getFullTextFetchers(importFormatPreferences, importerPreferences);

        try (ScanResult scanResult = classGraph.scan()) {
            ClassInfoList controlClasses = scanResult.getClassesImplementing(FulltextFetcher.class.getCanonicalName());
            Set<Class<?>> expected = new HashSet<>(controlClasses.loadClasses());

            // Remove the following, because they don't work atm
            expected.remove(JstorFetcher.class);
            expected.remove(GoogleScholar.class);

            assertEquals(expected, getClasses(fullTextFetchers));
        }
    }

    @Test
    void getIdFetchersReturnsAllFetcherDerivingFromIdFetcher() {
        Set<IdFetcher<?>> idFetchers = WebFetchers.getIdFetchers(importFormatPreferences);

        try (ScanResult scanResult = classGraph.scan()) {
            ClassInfoList controlClasses = scanResult.getClassesImplementing(IdFetcher.class.getCanonicalName());
            Set<Class<?>> expected = new HashSet<>(controlClasses.loadClasses());

            expected.remove(IdParserFetcher.class);
            // Remove the following, because they don't work at the moment
            expected.remove(GoogleScholar.class);

            assertEquals(expected, getClasses(idFetchers));
        }
    }

    private Set<? extends Class<?>> getClasses(Collection<?> objects) {
        return objects.stream().map(Object::getClass).collect(Collectors.toSet());
    }
}
