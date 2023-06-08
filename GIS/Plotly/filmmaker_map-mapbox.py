############### POOR PERFORMANCE WARNING ###############

import plotly.express as px
import pandas as pd

GROUP_BY_WHETHER_DEBUT_FROM_PRIVATE_STUDIOS = True

import plotly
plotly.__version__

px.set_mapbox_access_token(open("GIS/Plotly/mapbox_token.txt").read())
df = pd.read_csv("GIS/source/people_plots(geographical).csv")

if(GROUP_BY_WHETHER_DEBUT_FROM_PRIVATE_STUDIOS == False):
	fig = px.scatter_mapbox(df, lat="lat", lon="long",
						color_continuous_scale="matter",
						color="dg",
						color_discrete_map={
							"Shanghai (state)": "#2596be",#green
							"Shanghai (private)" : "#404840",#green black
							"Shanghai (roc)" : "#96fb96",#light green
							"Shanghai (state) / Hong Kong" : "#2596be", #dark green
							"Beijing": "#D063fb",#hliotrope
							"Beijing / Hong Kong" : "#F3c0f9",#light purple
							"Northeast": "#72d2ed",#sky blue
							"Xi'an": "#Fb9f68",#tan orange
							"Canton": "#Fb94cc",#lavender pink
							"Hong Kong / Canton": "#F5aedc",#chantilly
							"Xinjiang": "#C83b3b",#dark red
							"Sichuan" : "#2c6398"#tropaz blue
						},
						#hover_name="name",
						zoom=3,
						animation_frame="yr", animation_group="id",
						mapbox_style="mapbox://styles/el-mundo/clgse9vm7001x01p6hrli21dq")
else:
      fig = px.scatter_mapbox(df, lat="lat", lon="long",
						color_continuous_scale="matter",
						color="pri",
						color_discrete_map={
							"true": "#2596be",#green
							"false" : "#C83b3b"#red
						},
						#hover_name="name",
						zoom=3,
						animation_frame="yr", animation_group="id",
						mapbox_style="mapbox://styles/el-mundo/clgse9vm7001x01p6hrli21dq")

fig.update_traces(cluster=dict(enabled=True))
fig.update_layout(height = 800, width = 1280, margin={"r": 10, "t": 10, "l": 10, "b": 10})
fig.write_html("GIS/Plotly/filmmaker_map-mapbox.html")
fig.show()