Kindlet_flight_log
==================
Flight logger for Highland Aerosports on a Kindle Touch.

UI Operations
-- Main Menu --
	Menu - Menu in the top right corner
		Save  - Saves the current log state
		Sync - Suncs current log to dropbox
		Update - updates config files from the Dropbox account
	Table Logs - Tables containing flight information
		Duplicate - Hold flight number cell to recieve dialog for duplication
			Delete - Cancel Duplicate Dialog to recieve dialog for deletion
		Edit - Modify flight Data
			Edit Name - Hold Pilots Name to Modify it
			Edit Other Fields - edit altitude, crew, and special notes
		Scroll Pages - More than 20 flights allow swipes to scroll
		Add Buttons - Starts creating a new flight either solo or tandem
			Duplicate - Holding the button will duplicate the last flight
	Name Page - Editing the name occurs on this page
		Cancel - press button to cancel adding or editing
		Use Name - press button with name to select the name
			Recent Names - initially the add dialog shows recently used names
			without duplication
			Similar Names - once you begin to type the possible names are
			retrieved from the database
		Use Custom Name - press return on the keyboard to select the custom
		name
	Alt/Notes/Crew Page - This page contains info about the crew. Defaults are
	designated * for the tug pilot and @ for the tandem pilot before their name
	in the config/crew.csv file
