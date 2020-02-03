package com.coremedia.blueprint.studio.feedbackhub.searchmetrics.components {
import com.coremedia.ui.data.ValueExpression;

import ext.button.Button;

import ext.container.Container;

[ResourceBundle('com.coremedia.blueprint.studio.feedbackhub.searchmetrics.FeedbackHubSearchmetrics')]
public class DuplicatePanelBase extends Container {

  [Bindable]
  public var bindTo:ValueExpression;

  public function DuplicatePanelBase(config:DuplicatePanel = null) {
    super(config);
  }

  protected function formatPercentage(value:Number):String {
    return value + "%";
  }

  protected function linkClick(b:Button):void {
    var url:String = b.getText();
    window.open(url, '_blank');
  }
}
}
