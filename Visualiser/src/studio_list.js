var studio_search_input = '';

let studios_shown = [];
var studio_scroll = 0;
var max_studio_scroll = 0;

var studio_search_tag = "Showing all studios in dataset:";
var studio_search_tag_chn = "显示数据集中的所有制片厂：";

const STUDIO_HEIGHT = 100;

var studio_search_button, clear_studio_search_button;
var studio_to_map_button, studio_to_name_button, studio_to_cat_button;
let previewed_studio;

function draw_studio_page() {
	frame_offset_y = studio_scroll;
	//Draw all buttons in films_shown
	fill(255);
	textAlign(LEFT, CENTER);
	textSize(24);
	text((lan === 'chn' ? studio_search_tag_chn : studio_search_tag), 24, STUDIO_HEIGHT / 2);

	previewed_studio = null;
	highlited_regions.splice(0, highlited_regions.length);
	for(let i = 0; i < studios_shown.length; i++) {
		studios_shown[i].update();
	}
}

function draw_studio_bottom_bar() {
	fill(0);
	rect(0, DEFAULT_HEIGHT - CATALOGUE_BOTTOM_BAR_HEIGHT, DEFAULT_WIDTH, CATALOGUE_BOTTOM_BAR_HEIGHT);
	studio_search_button.update();
	clear_studio_search_button.update();
	studio_to_map_button.update();
	studio_to_name_button.update();
	studio_to_cat_button.update();
}

function draw_studio_scroll() {
	//Update catalogue scroll
	fill(255);
	rect(width-CATALOGUE_SCROLLER_WIDTH, -64, CATALOGUE_SCROLLER_WIDTH, height+64);

	if(lan==='chn') textFont(font_chn);
	else textFont('Arial');
	fill(200);
	if(mouseX > width-CATALOGUE_SCROLLER_WIDTH && mouseY > 0 && mouseY < height) {
		if(clicked) {
			if(max_names_scroll <= -DEFAULT_HEIGHT) {
				fill(128);
				studio_scroll = map(mouseY-CATALOGUE_SLIDER_SIZE/2, 0, height-CATALOGUE_SLIDER_SIZE, 0, max_studio_scroll);
				if(studio_scroll > 0) studio_scroll = 0;
				else if(studio_scroll < max_studio_scroll) studio_scroll = max_studio_scroll;
			}
		}
	}
	//Draw scroll bar
	rect(width-CATALOGUE_SCROLLER_WIDTH+2, map(studio_scroll, 0, max_studio_scroll, 0, height-CATALOGUE_SLIDER_SIZE), CATALOGUE_SCROLLER_WIDTH-4, CATALOGUE_SLIDER_SIZE, 3);
}

function show_all_names_in_studio_search() {
	studio_search_button = new StudioSearchButton(DEFAULT_WIDTH/4+DEFAULT_WIDTH*2/64, DEFAULT_HEIGHT-CATALOGUE_BOTTOM_BAR_HEIGHT*0.85, 128, CATALOGUE_BOTTOM_BAR_HEIGHT * 0.7, null, 1, "搜索", "Search");
	clear_studio_search_button = new ClearStudioSearchButton(DEFAULT_WIDTH/4+DEFAULT_WIDTH*2/64+136, DEFAULT_HEIGHT-CATALOGUE_BOTTOM_BAR_HEIGHT*0.85, 128, CATALOGUE_BOTTOM_BAR_HEIGHT * 0.7, null, 1, "重置", "Clear");
	studio_to_map_button = new ToMapButton(DEFAULT_WIDTH-CATALOGUE_SCROLLER_WIDTH-24-136*3, DEFAULT_HEIGHT-CATALOGUE_BOTTOM_BAR_HEIGHT*0.85, 128, CATALOGUE_BOTTOM_BAR_HEIGHT * 0.7, null, 1, "地图检索", "Map Filter");
	studio_to_name_button = new ToStaffButton(DEFAULT_WIDTH-CATALOGUE_SCROLLER_WIDTH-24-136*2, DEFAULT_HEIGHT-CATALOGUE_BOTTOM_BAR_HEIGHT*0.85, 128, CATALOGUE_BOTTOM_BAR_HEIGHT * 0.7, null, 1, "人名检索", "Staff Search");
	studio_to_cat_button = new ToCatalogueButton(DEFAULT_WIDTH-CATALOGUE_SCROLLER_WIDTH-24-136, DEFAULT_HEIGHT-CATALOGUE_BOTTOM_BAR_HEIGHT*0.85, 128, CATALOGUE_BOTTOM_BAR_HEIGHT * 0.7, null, 1, "影片目录", "Film List");
	search_field.position(width/64, height-height/24*1.7);
	search_field.size(width/4, height/24);
	search_field.value('');
	search_field.show();
	update_shown_studios();
}

function search_in_studios(input) {
	studio_search_input = input;
	update_shown_studios();
}

function update_shown_studios() {
	studio_scroll = 0;

	studios_shown.splice(0, studios_shown.length);
	let to_show = 0;
	for(let [st_n, s] of studios) {
		if(studio_search_input.length > 0) {
			if(s.name.includes(studio_search_input) || s.chn.includes(studio_search_input) || s.eng.includes(studio_search_input) || s.region.includes(studio_search_input)) {
				add_shown_studio(s, to_show);
				to_show++;
				continue;
			}
		}else{
			add_shown_studio(s, to_show);
			to_show++;
			continue;
		}
	}
	if(studio_search_input.length > 0) {
		studio_search_tag = "Showing studios with search term: " + name_search_input;
		studio_search_tag_chn = "显示搜索词：" + name_search_input;
	}else{
		studio_search_tag = "Showing all studios in dataset:";
		studio_search_tag_chn = "显示数据集中的所有制片厂：";
	}
	max_studio_scroll = (studios_shown.length + 1) * -STUDIO_HEIGHT + DEFAULT_HEIGHT - CATALOGUE_BOTTOM_BAR_HEIGHT;
	if(max_studio_scroll > -DEFAULT_HEIGHT) max_studio_scroll = -DEFAULT_HEIGHT;
}

function add_shown_studio(s, row) {
	let c;
	let geocat = s.region;
	if(GEO_CATEGORY_COLOURS.has(geocat)) {
		c = GEO_CATEGORY_COLOURS.get(geocat);
	}else{
		c = color(255, 255, 255);
	}
	let eng = s.eng;
	let chn = s.chn;
	let cap_size = 20;
	studios_shown.push(new StudioSelectButton(0, (row+1) * STUDIO_HEIGHT, DEFAULT_WIDTH-CATALOGUE_SCROLLER_WIDTH-MAP_PREVIEW_WIDTH, STUDIO_HEIGHT, null, 1, s.name, chn, eng, c, cap_size));
}

function preview_studio(studio) {
	previewed_studio = studio;
	let regions = [studio.region];
	draw_region_preview(regions);
}

function draw_studio_preview() {
	if(previewed_studio === null || previewed_studio === undefined) return;

	let useChn = lan === 'chn';
	textSize(20);
	fill(255);
	textAlign(CENTER, TOP);
	let expX1 = DEFAULT_WIDTH-MAP_PREVIEW_WIDTH-CATALOGUE_SCROLLER_WIDTH;
	let mapHei = 100 + MAP_PREVIEW_WIDTH;
	let cenHei = mapHei + (DEFAULT_HEIGHT - CATALOGUE_BOTTOM_BAR_HEIGHT - mapHei) * 0.5;
	
	let str;
	if(useChn) textFont(font_chn);
	else textFont('Arial');
	let src = useChn ? previewed_studio.chn : previewed_studio.eng;
	str = src;
	let y = cenHei;
	if(src.length > 50) {
		y -= 77;
		str = src.substring(0, 25);
		text(str + "-", expX1+MAP_PREVIEW_WIDTH*0.5, y);
		y += 22;
		str = src.substring(25, 50);
		text(str + "-", expX1+MAP_PREVIEW_WIDTH*0.5, y);
		y += 22;
		str = src.substring(50, src.length);
		text(str, expX1+MAP_PREVIEW_WIDTH*0.5, y);
	}else if(src.length > 25) {
		y -= 55;
		str = src.substring(0, 25);
		text(str + "-", expX1+MAP_PREVIEW_WIDTH*0.5, y);
		y += 22;
		str = src.substring(25, src.length);
		text(str, expX1+MAP_PREVIEW_WIDTH*0.5, y);
	}else{
		y -= 33;
		text(str, expX1+MAP_PREVIEW_WIDTH*0.5, y);
	}
	y += 22;
	if(useChn) {
		str = '电影总数： ' + previewed_studio.filmCount + ' 部';
	} else
		str = 'Film count: ' + previewed_studio.filmCount;
	text(str,expX1+MAP_PREVIEW_WIDTH*0.5, y);
}