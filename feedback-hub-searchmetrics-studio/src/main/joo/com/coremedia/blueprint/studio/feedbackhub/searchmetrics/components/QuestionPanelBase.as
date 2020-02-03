package com.coremedia.blueprint.studio.feedbackhub.searchmetrics.components {
import com.coremedia.blueprint.studio.feedbackhub.searchmetrics.model.Briefing;
import com.coremedia.ui.data.ValueExpression;

import ext.container.Container;

[ResourceBundle('com.coremedia.blueprint.studio.feedbackhub.searchmetrics.FeedbackHubSearchmetrics')]
public class QuestionPanelBase extends Container {

  [Bindable]
  public var bindTo:ValueExpression;

  [Bindable]
  public var briefing:Briefing;

  public function QuestionPanelBase(config:QuestionPanel = null) {
    super(config);
  }

  protected function groupTransformer(name:String):String {
    if(name === null) {
      name = resourceManager.getString('com.coremedia.blueprint.studio.feedbackhub.searchmetrics.FeedbackHubSearchmetrics', 'searchmetrics_other_questions_' + briefing.getLanguage());
      if(!name) {
        name = resourceManager.getString('com.coremedia.blueprint.studio.feedbackhub.searchmetrics.FeedbackHubSearchmetrics', 'searchmetrics_other_questions');
      }
    }
    return name;
  }
}
}
