/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * (C) Copyright 2009-2024, Arnaud Roques
 *
 * Project Info:  https://plantuml.com
 * 
 * If you like this project or if you find it useful, you can support us at:
 * 
 * https://plantuml.com/patreon (only 1$ per month!)
 * https://plantuml.com/paypal
 * 
 * This file is part of PlantUML.
 *
 * PlantUML is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PlantUML distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 *
 * Original Author:  Arnaud Roques
 * 
 *
 */
package net.sourceforge.plantuml.project.lang;

import net.sourceforge.plantuml.project.DaysAsDates;
import net.sourceforge.plantuml.project.Failable;
import net.sourceforge.plantuml.project.GanttDiagram;
import net.sourceforge.plantuml.project.time.Day;
import net.sourceforge.plantuml.regex.IRegex;
import net.sourceforge.plantuml.regex.RegexConcat;
import net.sourceforge.plantuml.regex.RegexLeaf;
import net.sourceforge.plantuml.regex.RegexOr;
import net.sourceforge.plantuml.regex.RegexResult;

public class ComplementIntervals implements Something<GanttDiagram> {

	public IRegex toRegex(String suffix) {
		return new RegexOr(toRegexB(suffix), toRegexE(suffix));
	}

	private IRegex toRegexB(String suffix) {
		final DayPattern dayPattern1 = new DayPattern("1");
		final DayPattern dayPattern2 = new DayPattern("2");
		return new RegexConcat( //
				dayPattern1.toRegex(), //
				Words.exactly(Words.TO), //
				Words.zeroOrMore(Words.THE), //
				RegexLeaf.spaceOneOrMore(), //
				dayPattern2.toRegex() //
		);
	}

	private IRegex toRegexE(String suffix) {
		return new RegexConcat( //
				new RegexLeaf("[dD]\\+"), //
				new RegexLeaf(1, "ECOUNT1" + suffix, "([\\d]+)"), //
				Words.exactly(Words.TO), //
				Words.zeroOrMore(Words.THE), //
				RegexLeaf.spaceOneOrMore(), //
				new RegexLeaf("[dD]\\+"), //
				new RegexLeaf(1, "ECOUNT2" + suffix, "([\\d]+)") //
		);
	}

	public Failable<DaysAsDates> getMe(GanttDiagram project, RegexResult arg, String suffix) {
		final Day d1 = new DayPattern("1").getDay(arg);
		if (d1 != null) {
			final Day d2 = new DayPattern("2").getDay(arg);
			return Failable.ok(new DaysAsDates(d1, d2));
		}

		if (arg.get("ECOUNT1" + suffix, 0) != null)
			return Failable.ok(resultE(project, arg, suffix));

		throw new IllegalStateException();

	}

	private DaysAsDates resultE(GanttDiagram project, RegexResult arg, String suffix) {
		final int day1 = Integer.parseInt(arg.get("ECOUNT1" + suffix, 0));
		final Day date1 = project.getStartingDate().addDays(day1);

		final int day2 = Integer.parseInt(arg.get("ECOUNT2" + suffix, 0));
		final Day date2 = project.getStartingDate().addDays(day2);

		return new DaysAsDates(date1, date2);
	}

}
