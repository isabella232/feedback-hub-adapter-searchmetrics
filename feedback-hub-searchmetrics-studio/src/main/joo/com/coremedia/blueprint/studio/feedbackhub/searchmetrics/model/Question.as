package com.coremedia.blueprint.studio.feedbackhub.searchmetrics.model {
import com.coremedia.blueprint.studio.feedbackhub.searchmetrics.SearchmetricsPropertyNames;
import com.coremedia.ui.data.impl.BeanImpl;

public class Question extends BeanImpl {

  public function Question(json:Object) {
    super(json);
  }

  public function getGroup():String {
    return get(SearchmetricsPropertyNames.GROUP);
  }
}
}
