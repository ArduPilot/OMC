var xmlSkinData = "";
xmlSkinData += '<?xml version=\"1.0\" encoding=\"utf-8\"?>';
xmlSkinData += '<CatapultSkin Version=\"1\" Comment=\"This is the default skin\" Anchors=\"Width,Height\" Width=\"800px\" Height=\"600px\" Top=\"0px\" Left=\"0px\" Bottom=\"0px\" Right=\"0px\" Tabs=\"TOC,Index,Search,Favorites\" DefaultTab=\"TOC\" UseBrowserDefaultSize=\"True\" UseDefaultBrowserSetup=\"True\" Title=\"MrSID Encode SDK User Manual\" conditions=\"Default.SharedWithAll\">';
xmlSkinData += '    <Index BinaryStorage=\"True\" />';
xmlSkinData += '    <HtmlHelpOptions ShowMenuBar=\"False\" TopmostWindowStyle=\"False\" Buttons=\"Hide,Locate,Back,Forward,Stop,Refresh,Home,Font,Print\" EnableButtonCaptions=\"True\" />';
xmlSkinData += '    <Stylesheet Link=\"Stylesheet.xml\">';
xmlSkinData += '    </Stylesheet>';
xmlSkinData += '    <WebHelpOptions NavigationPaneWidth=\"250\" />';
xmlSkinData += '    <Toolbar EnableCustomLayout=\"true\" Buttons=\"AddTopicToFavorites|ToggleNavigationPane|Print|Separator|Back|Forward|Stop|Refresh|Home|Separator|SelectIndex|SelectTOC|SelectGlossary|SelectSearch|RemoveHighlight|Separator|PreviousTopic|NextTopic\" />';
xmlSkinData += '    <TopicToolbar EnableCustomLayout=\"false\" Buttons=\"PreviousTopic|CurrentTopicIndex|NextTopic\" />';
xmlSkinData += '</CatapultSkin>';
CMCXmlParser._FilePathToXmlStringMap.Add('Skin', xmlSkinData);
