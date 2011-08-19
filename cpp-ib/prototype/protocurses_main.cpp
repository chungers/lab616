
extern "C"
{
#include <ncurses.h>
}

#include <iostream>
#include <string>
#include <stdio.h>

#include <gflags/gflags.h>
#include <glog/logging.h>

enum Colors {

  Red = 1,
  Green,
  Yellow,
  Reverse_Red,
  Reverse_Green,
  Reverse_Yellow
};

int main(int argc, char* argv[])
{

  google::SetUsageMessage("Prototype for ncurses");
  google::ParseCommandLineFlags(&argc, &argv, true);
  google::InitGoogleLogging(argv[0]);


  initscr();
  raw();
  keypad(stdscr, TRUE);
  noecho();
  
  if (has_colors() == FALSE) {
    endwin();
    std::cout << "No support for color!" << std::endl;
    exit(1);
  }

  start_color();
  init_pair(Red, COLOR_RED, COLOR_BLACK);
  init_pair(Green, COLOR_GREEN, COLOR_BLACK);
  init_pair(Yellow, COLOR_YELLOW, COLOR_BLACK);
  init_pair(Reverse_Red, COLOR_BLACK, COLOR_RED);
  init_pair(Reverse_Green, COLOR_BLACK, COLOR_GREEN);
  init_pair(Reverse_Yellow, COLOR_BLACK, COLOR_YELLOW);
  

  attron(COLOR_PAIR(Red) | A_UNDERLINE);
  printw("Hello World.");
  attroff(COLOR_PAIR(Red) | A_UNDERLINE);

  move(1, 30);
  attron(A_REVERSE | A_BLINK);
  printw("Blink!");
  attroff(A_REVERSE | A_BLINK);

  for (int i=1; i < 10; ++i) {

    attron(A_BOLD);
    attron(COLOR_PAIR(Green));
    mvwprintw(stdscr, 5, 10, "%d", i);
    attroff(COLOR_PAIR(Green));
    attroff(A_BOLD);
    
    Colors c = (i % 2 == 0) ? Red : Reverse_Red;

    attron(COLOR_PAIR(c));
    mvwprintw(stdscr, 5, 15, "%d", i);
    attroff(COLOR_PAIR(c));


    attron(A_STANDOUT | COLOR_PAIR(Yellow));
    mvwprintw(stdscr, 5, 20, "%d", i);
    attroff(A_STANDOUT | COLOR_PAIR(Yellow));

    attron(COLOR_PAIR(Reverse_Green));
    mvwprintw(stdscr, 5, 25, "%d", i);
    attroff(COLOR_PAIR(Reverse_Green));

    
    move(10, 10);
    refresh();

    sleep(1);
  }

  echo();
  int ch = getch();
  if (ch == 'q') {

  }
  sleep(2);
  endwin();

  return 0;
}
