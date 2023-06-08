import plotly.express as px
import pandas as pd
import plotly.graph_objects as go

###Can be intensity_sum,level_sum,intensity_avg,intensity_avg
ATTRIBUTE = "intensity_avg"
###Can be "category", "production", "year", or "type"
GROUPING = "category"
###Can be "chunk_cen", "chunk_st" or "chunk_end"
X_AXIS = "chunk_cen"

ANIMATE = False

###Whether show category as GeoCategories or Studios
#CATEGORIZE_BY_GEO_CATEGORY= True

###Can be "Feature", "Performance", "Artistic Documentary", "Opera", or "Musical"
###Set as None to show all types
TYPE_FILTER = ""

###Show the frames with the maximum heads of each film only
###Only supports all types or features
#MAX_ONLY = False

###Chinese films (default), or NON_CHN
NON_CHN = False









tarpath = "CV/emotion/narrative-nonchn.csv"

#if MAX_ONLY:
#    tarpath = tarpath.replace(".csv", "-max.csv")

df = pd.read_csv(tarpath)

cap = "Crowd Sizes" if ATTRIBUTE == "num_people" or ATTRIBUTE == "num_heads" else "Normalized Crowd Sizes"
#if MAX_ONLY:
#    cap = "Crowds with Maximum Head Count"

# Display the data frame in Plotly scatter plot
if not ANIMATE:
	fig = px.histogram(df, x=X_AXIS, y=ATTRIBUTE, histfunc='avg', color=GROUPING, hover_name="title", hover_data=["title","chunk_st"], title="{} Distributed in {}Films".format(cap, "All " if TYPE_FILTER is None else f"{TYPE_FILTER} ")
                    ,
					color_discrete_map = {
						"Shanghai (state)": "#68d162",#green
						"Shanghai (private)" : "#404840",#green black
						"Beijing": "#D063fb",#hliotrope
						"Northeast": "#72d2ed",#sky blue
						"Xi'an": "#Fb9f68",#tan orange
						"Canton": "#Fb94cc",#lavender pink
						"Soviet Union": "#C83b3b",#dark red
						"Hollywood" : "#2c6398",#tropaz blue
					}, barmode="group")

	# multi bar
	#fig = go.Figure(data = [
    #        go.Bar(name = 'Shanghai (state)', x = df['chunk_cen'], y = df[df['category'] == 'Shanghai (state)'][ATTRIBUTE], marker_color = '#68d162'),
    #        go.Bar(name = 'Shanghai (private)', x = df['chunk_cen'], y = df[df['category'] == 'Shanghai (private)'][ATTRIBUTE], marker_color = '#404840'),
    #        go.Bar(name = 'Beijing', x = df['chunk_cen'], y = df[df['category'] == 'Beijing'][ATTRIBUTE], marker_color = '#D063fb'),
    #        go.Bar(name = 'Northeast', x = df['chunk_cen'], y = df[df['category'] == 'Northeast'][ATTRIBUTE], marker_color = '#72d2ed'),
    #        go.Bar(name = 'Xi\'an', x = df['chunk_cen'], y = df[df['category'] == 'Xi\'an'][ATTRIBUTE], marker_color = '#Fb9f68'),
    #        go.Bar(name = 'Canton', x = df['chunk_cen'], y = df[df['category'] == 'Canton'][ATTRIBUTE], marker_color = '#Fb94cc'),
    #        go.Bar(name = 'Soviet Union', x = df['chunk_cen'], y = df[df['category'] == 'Soviet Union'][ATTRIBUTE], marker_color = '#C83b3b'),
    #        go.Bar(name = 'Hollywood', x = df['chunk_cen'], y = df[df['category'] == 'Hollywood'][ATTRIBUTE], marker_color = '#2c6398')
	#])
	#fig.update_layout(barmode = 'group', title = "{} Distributed in {}Films".format(cap, "All " if TYPE_FILTER is None else f"{TYPE_FILTER} "), xaxis_title = "Year", yaxis_title = "Average Head Count", legend_title = "Category", title_x = 0.5)

else:
     REGIONS = ['Shanghai (state)', 'Beijing', 'Northeast', "Xi'an", "Canton", "Hollywood", "Soviet Union"]
     for i, n in enumerate(REGIONS):
        for year in range(1949, 1967):
              new_row = {'film':' ', 'title':' ', 'chunk_start':-1.0, 'chunk_end':-1.0, 'head_sum':0, 'people_sum':0, 'head_avg':0, 'people_avg':0, 'category':n, 'production':' ', 'year':year, 'type':' '}
              df = pd.concat([df, pd.DataFrame(new_row, index=[0])], ignore_index=True)
			  
     df.sort_values(by="year", inplace=True)
     max = 1 if ATTRIBUTE == "n_heads" else 200
     fig = px.histogram(df, x=X_AXIS, y=ATTRIBUTE, histfunc='avg', color=GROUPING, hover_name="title", hover_data=["title","chunk_st"], title="{} Distributed in {}Films".format(cap, "All " if TYPE_FILTER is None else f"{TYPE_FILTER} ")
                      , animation_frame="year", range_x=[0,1], range_y=[0,max], nbins=20,
					color_discrete_map = {
						"Shanghai (state)": "#68d162",#green
						"Shanghai (private)" : "#404840",#green black
						"Beijing": "#D063fb",#hliotrope
						"Northeast": "#72d2ed",#sky blue
						"Xi'an": "#Fb9f68",#tan orange
						"Canton": "#Fb94cc",#lavender pink
						"Soviet Union": "#C83b3b",#dark red
						"Hollywood" : "#2c6398",#tropaz blue
					})

#Set X axis title "Average Head Count" and legend title "Category"
#fig.update_layout(yaxis_title="Average Head Count", legend_title="Category", title_x=0.5) 
	
fig.show()