function draw_loading_scene() {
	clear();
	fill('#ED225D');
	//textFont(font_chn);
	textAlign(CENTER, CENTER);
	textSize(32);
	text('Loading resources (' + loaded_resource + '/' + RESOURCE_NUM + ')...', width/2, height/2);
}