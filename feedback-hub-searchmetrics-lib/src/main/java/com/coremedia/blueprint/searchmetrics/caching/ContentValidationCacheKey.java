package com.coremedia.blueprint.searchmetrics.caching;

import com.coremedia.blueprint.searchmetrics.SearchmetricsConnector;
import com.coremedia.blueprint.searchmetrics.SearchmetricsSettings;
import com.coremedia.blueprint.searchmetrics.documents.Briefing;
import com.coremedia.blueprint.searchmetrics.documents.ContentValidation;
import com.coremedia.blueprint.searchmetrics.documents.QueryResult;
import com.coremedia.blueprint.searchmetrics.queries.ValidateContentQuery;
import com.coremedia.cache.Cache;
import com.coremedia.cache.CacheKey;
import com.coremedia.cap.content.Content;
import com.coremedia.xml.Markup;
import com.coremedia.xml.MarkupUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 *
 */
public class ContentValidationCacheKey extends CacheKey<ContentValidation> {
  private static final Logger LOG = LoggerFactory.getLogger(ContentValidationCacheKey.class);
  private static final String DEFAULT_PROPERTY_NAME = "detailText";
  private static final String SUBJECT_TAXONOMY = "subjectTaxonomy";
  private static final String LOCATION_TAXONOMY = "locationTaxonomy";
  private static final String VALUE = "value";
  private static final String KEYWORDS = "keywords";

  private SearchmetricsConnector connector;
  private SearchmetricsSettings settings;
  private Briefing briefing;
  private Content content;
  private String text;

  public ContentValidationCacheKey(@NonNull SearchmetricsConnector connector,
                                   @NonNull SearchmetricsSettings settings,
                                   @NonNull Briefing briefing,
                                   @NonNull Content content) {
    this.connector = connector;
    this.settings = settings;
    this.briefing = briefing;
    this.content = content;
    this.text = getTextContent(content, settings.getPropertyName(), settings.getIncludeKeywords(), settings.getIncludeTaxonomies());
  }

  @Override
  public ContentValidation evaluate(Cache cache) {
    String input = text.replaceAll("\"", "").trim();
    ValidateContentQuery query = new ValidateContentQuery(briefing.getId(), input);
    Optional<QueryResult> queryResult = connector.postResource(settings, query.toString(), QueryResult.class);
    if (queryResult.isPresent()) {
      return queryResult.get().getData().getContentValidation();
    }

    LOG.info("Empty detail text for {}, skipped searchmetrics analysis", content.getPath());
    return new ContentValidation();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ContentValidationCacheKey)) {
      return false;
    }

    ContentValidationCacheKey that = (ContentValidationCacheKey) o;
    return this.text.equals(that.text)
            && this.briefing.getId().equals(that.briefing.getId())
            && this.settings.getProjectId().equals(that.settings.getProjectId());
  }

  @Override
  public int hashCode() {
    return this.text.hashCode();
  }

  private String getTextContent(Content content, String propertyName, Boolean includeKeywords, Boolean includeTaxonomies) {
    if (propertyName == null) {
      propertyName = DEFAULT_PROPERTY_NAME;
    }

    Map<String, Object> properties = content.getProperties();
    Set<Map.Entry<String, Object>> entries = properties.entrySet();
    String contentText = briefing.getTitle();
    for (Map.Entry<String, Object> entry : entries) {
      if (entry.getKey().equals(propertyName)) {
        Markup markup = (Markup) entry.getValue();
        if (markup != null) {
          contentText = MarkupUtil.asPlainText(markup).trim();
        }
        break;
      }
    }

    if (includeTaxonomies != null && includeTaxonomies) {
      contentText = contentText + getTaxonomyKeywords(content, SUBJECT_TAXONOMY);
      contentText = contentText + getTaxonomyKeywords(content, LOCATION_TAXONOMY);
    }

    if (includeKeywords != null && includeKeywords) {
      contentText = contentText + getKeywords(content, KEYWORDS);
    }

    if(contentText == null) {
      contentText = "";
    }

    return contentText;
  }

  private String getTaxonomyKeywords(Content content, String taxonomyName) {
    Map<String, Object> properties = content.getProperties();
    Set<Map.Entry<String, Object>> entries = properties.entrySet();

    StringBuilder builder = new StringBuilder();
    for (Map.Entry<String, Object> entry : entries) {
      if (entry.getKey().equals(taxonomyName)) {
        List<Content> taxonomies = (List<Content>) entry.getValue();
        if (taxonomies != null) {
          for (Content taxonomy : taxonomies) {
            String name = taxonomy.getString(VALUE);
            if (StringUtils.isEmpty(name)) {
              name = taxonomy.getName();
            }
            builder.append(" ");
            builder.append(name);
          }
        }
      }
    }

    return builder.toString();
  }

  private String getKeywords(Content content, String propertyName) {
    Map<String, Object> properties = content.getProperties();
    Set<Map.Entry<String, Object>> entries = properties.entrySet();

    StringBuilder builder = new StringBuilder();
    for (Map.Entry<String, Object> entry : entries) {
      if (entry.getKey().equals(propertyName)) {
        String value = (String) entry.getValue();
        if (!StringUtils.isEmpty(value)) {
          String[] keywords = value.trim().split(" ");
          for (String keyword : keywords) {
            if (keyword.trim().length() > 0) {
              builder.append(" ");
              builder.append(keyword.trim());
            }
          }
        }
      }
    }

    return builder.toString();
  }
}
