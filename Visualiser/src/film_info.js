var display_film_key = 'qiao';
var film;

let production, title, type, year;
let colour, reels;
let directors, scriptwriters, actors, otherStaff;
let plot_summary;

let back_to_catalogue_button, open_other_staff_button, hide_other_staff_button;
var showing_ohter_staff_page = false;

function draw_film_page() {
	frame_offset_y = 0;
	textAlign(LEFT, TOP);

	//Draw film title
	textWrap(WORD);
	textSize(32);
	fill(255, 255, 220, 200);
	rect(0, 8, DEFAULT_WIDTH-CATALOGUE_SCROLLER_WIDTH-MAP_PREVIEW_WIDTH, 48, 5);
	fill(0);
	text(title + ' (' + year + ')', 12, 12);
	fill(202, 255, 201, 200);
	rect(0, 56, DEFAULT_WIDTH-CATALOGUE_SCROLLER_WIDTH-(display_film_key==='dong fang hong' ? 0:MAP_PREVIEW_WIDTH), 36, 5);
	textSize(20);
	fill(0);
	text(production + ' - ' + type, 12, 64);
	fill(217, 244, 255, 200);
	rect(0, 92, DEFAULT_WIDTH-CATALOGUE_SCROLLER_WIDTH-MAP_PREVIEW_WIDTH-256, 34, 5);
	fill(0);
	text(colour + ', ' + reels, 12, 96);
	textSize(16);
	fill(255);
	let string = directors + '\n' + scriptwriters;
	text(string, 12, 144, DEFAULT_WIDTH - CATALOGUE_SCROLLER_WIDTH - MAP_PREVIEW_WIDTH - 24);
	//textWrap((lan === 'chn') ? CHAR : WORD); //Char Wraping in the lib is broken
	fill(255);
	if(plot_summary.length > 2400) {
		textSize(12);
		text((lan === 'chn' ? '剧 情 简 介' : 'Plot Summary'), 12, 200);
		text(plot_summary, 12, 216, DEFAULT_WIDTH - CATALOGUE_SCROLLER_WIDTH - MAP_PREVIEW_WIDTH - 24);
	}else{
		textSize(14);
		text((lan === 'chn' ? '剧 情 简 介' : 'Plot Summary'), 12, 202);
		text(plot_summary, 12, 216, DEFAULT_WIDTH - CATALOGUE_SCROLLER_WIDTH - MAP_PREVIEW_WIDTH - 24);
	}
	if(showing_ohter_staff_page) {
		fill(0, 220);
		rect(-1, -1, DEFAULT_WIDTH+1, DEFAULT_HEIGHT+1);
		fill(255, 255, 224, 200);
		rect(24, 24, DEFAULT_WIDTH - 256, DEFAULT_HEIGHT - 128, 20);
		textSize(16);
		textWrap(WORD);
		fill(0);
		if(lan === 'chn')
			text('演员：\n' + actors + '\n\n其他职员：\n' + otherStaff, 48, 48, DEFAULT_WIDTH - 304);
		else
			text('Actors/Actresses:\n'+actors+'\n\nOther staff:\n'+otherStaff, 48, 48, DEFAULT_WIDTH - 304);
		hide_other_staff_button.update();
	}else{
		back_to_catalogue_button.update();
		open_other_staff_button.update();
	}
}

function show_film_page(film_key) {
	search_field.hide();

	display_film_key = film_key;
	film = filmMap.get(film_key);
	let useChn = (lan === 'chn');

	title = (useChn ? film.title : film.translated);
	if(title.includes('(aka')) title = title.substring(0, title.indexOf('(aka')-1);
	year = film.year;
	let productionObjs = film.production;
	let str = '';
	for(let i = 0; i < productionObjs.length; i++) {
		str = str.concat((useChn ? productionObjs[i].chn : productionObjs[i].eng) + ', ');
	}
	str = str.substring(0, str.length - 2);
	production = str;

	type = film.filmType;
	if(useChn) type = FILM_TYPES_CHN.get(type);

	if(!useChn) {
		reels = (film.reels < 0 ? 'unknown' : film.reels) + ' film reels';
		colour = film.colour.length < 1 ? 'Unknown colour' : film.colour;
	}else{
		reels = film.reels < 0 ? '胶卷本数不详' : film.reels + '本胶卷';
		colour = film.colour.length < 1 ? '色彩不详' : (film.colour === 'Colour' ? '彩色' : '黑白');
	}

	if(useChn) {
		directors = '导演：';
		for(let i = 0; i < film.directorNameArray.length; i++) {
			directors = directors.concat(film.directorNameArray[i] + ', ');
		}
		if(film.directorNameArray.length>0) directors = directors.substring(0, directors.length - 2);
		scriptwriters = '编剧：';
		for(let i = 0; i < film.scriptwriterNameArray.length; i++) {
			scriptwriters = scriptwriters.concat(film.scriptwriterNameArray[i] + ', ');
		}
		if(film.scriptwriterNameArray.length>0) scriptwriters = scriptwriters.substring(0, scriptwriters.length - 2);
		actors = '';
		for(let i = 0; i < film.actingNameArray.length; i++) {
			actors = actors.concat(film.actingNameArray[i] + ', ');
		}
		if(film.actingNameArray.length>0) actors = actors.substring(0, actors.length - 2);
		otherStaff = '';
		let osMap = new Map(Object.entries(film.otherStaffNameArrayWithRole));
		for (const [key, value] of osMap.entries()) {
			otherStaff = otherStaff.concat(key + '(' + value + '), ');
		}
		if(osMap.size>0) otherStaff = otherStaff.substring(0, otherStaff.length - 2);
		plot_summary = film.plot.replaceAll('{COMMA}', ',');
		plot_summary = plot_summary.replaceAll('{QUOTE}', '"');
		plot_summary = plot_summary.replaceAll('{LINE_CUT}', '\n');
		plot_summary = add_space(plot_summary);
	}else{
		directors = 'Director: ' + film.translatedDirectors;
		scriptwriters = 'Scriptwriter: ' + film.translatedScriptwriters;
		actors = '' + film.translatedActing;
		otherStaff = '' + film.translatedOtherStaff;
		plot_summary = film.translatedPlotSummary.replaceAll('{COMMA}', ',');
		plot_summary = plot_summary.replaceAll('{QUOTE}', '"');
		plot_summary = plot_summary.replaceAll('{LINE_CUT}', '\n');
	}

	back_to_catalogue_button = new BackToCatalogueButton(DEFAULT_WIDTH-128-CATALOGUE_SCROLLER_WIDTH, 12, 128, 48, null, 1, "返回", "Back");
	hide_other_staff_button = new CloseFilmInfoStaffPageButton(DEFAULT_WIDTH-128, 64, 64, 64, null, 1, "关闭", "Close");
	open_other_staff_button = new OpenFilmInfoStaffPageButton(DEFAULT_WIDTH-CATALOGUE_SCROLLER_WIDTH-MAP_PREVIEW_WIDTH/2-64, DEFAULT_HEIGHT-CATALOGUE_BOTTOM_BAR_HEIGHT-144, 128, 64, null, 1, "其他职员", "Other Staff");
}

function update_film_page() {
	if(display_film_key.length > 0) {
		show_film_page(display_film_key);
	}
}

function add_space(str) {
	return str.split('').join(' ');
}