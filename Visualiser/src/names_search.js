var name_search_input = '';

let names_shown = [];
var names_scroll = 0;
var max_names_scroll = 0;

var names_search_tag = "Showing all filmmakers/actors/actresses in dataset:";
var names_search_tag_chn = "显示数据集中的所有演职员：";

const NAME_LIST_WIDTH = 640;
const NAME_HEIGHT = 64;

var name_search_button, clear_name_search_button;
var name_to_map_button, name_to_studio_button, name_to_studio_button;
let preview_filmmaker_name = '', preview_filmmaker_region = '', preview_filmmaker_region_chn = '',
	preview_filmmaker_role = '', preview_filmmaker_role_chn = '';

function draw_name_search() {
	frame_offset_y = names_scroll;
	//Draw all buttons in films_shown
	fill(255);
	textAlign(LEFT, CENTER);
	textSize(24);
	text((lan === 'chn' ? names_search_tag_chn : names_search_tag), 24, NAME_HEIGHT / 2);

	preview_filmmaker_name = ''; preview_filmmaker_region = ''; preview_filmmaker_role = ''; preview_filmmaker_role_chn = '', preview_filmmaker_region_chn = '';
	highlited_regions.splice(0, highlited_regions.length);
	for(let i = 0; i < names_shown.length; i++) {
		names_shown[i].update();
	}
}

function draw_name_bottom_bar() {
	fill(0);
	rect(0, DEFAULT_HEIGHT - CATALOGUE_BOTTOM_BAR_HEIGHT, DEFAULT_WIDTH, CATALOGUE_BOTTOM_BAR_HEIGHT);
	name_search_button.update();
	clear_name_search_button.update();
	name_to_map_button.update();
	name_to_studio_button.update();
	name_to_cat_button.update();
}

function draw_name_scroll() {
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
				names_scroll = map(mouseY-CATALOGUE_SLIDER_SIZE/2, 0, height-CATALOGUE_SLIDER_SIZE, 0, max_names_scroll);
				if(names_scroll > 0) names_scroll = 0;
				else if(names_scroll < max_names_scroll) names_scroll = max_names_scroll;
			}
		}
	}
	//Draw scroll bar
	rect(width-CATALOGUE_SCROLLER_WIDTH+2, map(names_scroll, 0, max_names_scroll, 0, height-CATALOGUE_SLIDER_SIZE), CATALOGUE_SCROLLER_WIDTH-4, CATALOGUE_SLIDER_SIZE, 3);
}

function show_all_names_in_name_search() {
	name_search_button = new NameSearchButton(DEFAULT_WIDTH/4+DEFAULT_WIDTH*2/64, DEFAULT_HEIGHT-CATALOGUE_BOTTOM_BAR_HEIGHT*0.85, 128, CATALOGUE_BOTTOM_BAR_HEIGHT * 0.7, null, 1, "搜索", "Search");
	clear_name_search_button = new ClearNameSearchButton(DEFAULT_WIDTH/4+DEFAULT_WIDTH*2/64+136, DEFAULT_HEIGHT-CATALOGUE_BOTTOM_BAR_HEIGHT*0.85, 128, CATALOGUE_BOTTOM_BAR_HEIGHT * 0.7, null, 1, "重置", "Clear");
	name_to_map_button = new ToMapButton(DEFAULT_WIDTH-CATALOGUE_SCROLLER_WIDTH-24-136*3, DEFAULT_HEIGHT-CATALOGUE_BOTTOM_BAR_HEIGHT*0.85, 128, CATALOGUE_BOTTOM_BAR_HEIGHT * 0.7, null, 1, "地图检索", "Map Filter");
	name_to_studio_button = new ToStudioButton(DEFAULT_WIDTH-CATALOGUE_SCROLLER_WIDTH-24-136, DEFAULT_HEIGHT-CATALOGUE_BOTTOM_BAR_HEIGHT*0.85, 128, CATALOGUE_BOTTOM_BAR_HEIGHT * 0.7, null, 1, "制片厂检索", "Studio Index");
	name_to_cat_button = new ToCatalogueButton(DEFAULT_WIDTH-CATALOGUE_SCROLLER_WIDTH-24-136*2, DEFAULT_HEIGHT-CATALOGUE_BOTTOM_BAR_HEIGHT*0.85, 128, CATALOGUE_BOTTOM_BAR_HEIGHT * 0.7, null, 1, "影片目录", "Film List");
	search_field.position(width/64, height-height/24*1.7);
	search_field.size(width/4, height/24);
	search_field.value('');
	search_field.show();
	update_shown_names();
}

function search_in_names(input) {
	name_search_input = input;
	update_shown_names();
}

function update_shown_names() {
	names_scroll = 0;

	names_shown.splice(0, names_shown.length);
	let to_show = 0;
	let rowTotal = names_csv.getRowCount();
	for(let i = 0; i < rowTotal; i++) {
		let p = names_csv.getRow(i);
		if(name_search_input.length > 0) {
			if(p.get(1).includes(name_search_input)) {
				add_shown_name(p, to_show);
				to_show++;
				continue;
			}
		}else{
			add_shown_name(p, to_show);
			to_show++;
			continue;
		}
	}
	if(name_search_input.length > 0) {
		names_search_tag = "Showing names with search term: " + name_search_input;
		names_search_tag_chn = "显示搜索词：" + name_search_input;
	}else{
		names_search_tag = "Showing all filmmakers/actors/actresses in dataset:";
		names_search_tag_chn = "显示数据集中的所有演职员：";
	}
	max_names_scroll = (names_shown.length + 1) * -NAME_HEIGHT + DEFAULT_HEIGHT - CATALOGUE_BOTTOM_BAR_HEIGHT;
	if(max_names_scroll > -DEFAULT_HEIGHT) max_names_scroll = -DEFAULT_HEIGHT;
}

function add_shown_name(p, row) {
	let c;
	let geocat = p.get(2);
	if(GEO_CATEGORY_COLOURS.has(geocat)) {
		c = GEO_CATEGORY_COLOURS.get(geocat);
	}else{
		c = color(255, 255, 255);
	}
	let eng = (row+1) + '. ' + p.get(1);
	let chn = (row+1) + '. ' + p.get(1);
	let cap_size = 20;
	if(eng.length > 50) cap_size = 12;
	names_shown.push(new NameSelectButton(0, (row+1) * NAME_HEIGHT, NAME_LIST_WIDTH, NAME_HEIGHT, null, 1, p.get(0), chn, eng, c, cap_size));
}

function preview_filmmaker(rowObj) {
	preview_filmmaker_name = rowObj.get(1);
	preview_filmmaker_region = rowObj.get(2).replaceAll('Northeast', 'Jilin').replaceAll("Xi'an", 'Shaanxi').replaceAll("Inner Mongolia", "Inner_Mongolia");
	let regions = preview_filmmaker_region.split(' / ');
	for(let i = 0; i < regions.length; i++) {
		let r = regions[i];
		if(r.includes('Shanghai')) r = 'Shanghai';
		r = REGIONS_CHN.get(r);
		preview_filmmaker_region_chn = preview_filmmaker_region_chn.concat(r + ' / ');
	}
	draw_region_preview(regions);
	if(regions.length > 0) preview_filmmaker_region_chn = preview_filmmaker_region_chn.substring(0, preview_filmmaker_region_chn.length-3);
	preview_filmmaker_role_chn = rowObj.get(3);
	preview_filmmaker_role = rowObj.get(4);
}

function draw_name_preview() {
	let useChn = lan === 'chn';
	textSize(20);
	fill(255);
	textAlign(CENTER, TOP);
	let expWid = DEFAULT_WIDTH-CATALOGUE_SCROLLER_WIDTH-NAME_LIST_WIDTH;
	let mapHei = 100 + MAP_PREVIEW_WIDTH;
	let cenHei = mapHei + (DEFAULT_HEIGHT - CATALOGUE_BOTTOM_BAR_HEIGHT - mapHei) * 0.5;
	
	let str;
	if(useChn) textFont(font_chn);
	else textFont('Arial');
	str = preview_filmmaker_name;
	let y = cenHei;
	if(str.length > 50) {
		y -= 77;
		str = str.substring(0, 25);
		text(str + "-", NAME_LIST_WIDTH + expWid*0.5, y);
		y += 22;
		str = preview_filmmaker_name.substring(25, 50);
		text(str + "-", NAME_LIST_WIDTH + expWid*0.5, y);
		y += 22;
		str = preview_filmmaker_name.substring(50, preview_filmmaker_name.length);
		text(str, NAME_LIST_WIDTH + expWid*0.5, y);
	}else if(str.length > 25) {
		y -= 55;
		str = str.substring(0, 25);
		text(str + "-", NAME_LIST_WIDTH + expWid*0.5, y);
		y += 22;
		str = preview_filmmaker_name.substring(25, preview_filmmaker_name.length);
		text(str, NAME_LIST_WIDTH + expWid*0.5, y);
	}else{
		y -= 33;
		text(str, NAME_LIST_WIDTH + expWid*0.5, y);
	}
	y += 22;
	if(useChn)
		str = '主要活动地区： ' + preview_filmmaker_region_chn;
	else
		str = 'Main region: ' + preview_filmmaker_region;
	text(str, NAME_LIST_WIDTH + expWid*0.5, y);
	y += 22;
	if(useChn)
		str = '主要职位：' + preview_filmmaker_role_chn;
	else
		str = 'Main role: ' + preview_filmmaker_role;
	text(str, NAME_LIST_WIDTH + expWid*0.5, y);
}