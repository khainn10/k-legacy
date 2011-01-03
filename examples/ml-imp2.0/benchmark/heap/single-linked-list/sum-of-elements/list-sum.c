#include <stdlib.h>
#include <stdio.h>

struct nodeList {
  int val;
  struct nodeList *next;
};

int summ(struct nodeList* a)
{
  int s;
  struct nodeList* x;
  x = a;
  s = 0;
  while (x != 0) {
    s = s + x->val;
    x = x->next;
  }
  return s;
}

int main()
{
  struct nodeList* x;
  struct nodeList* y;
  x = (struct nodeList*)malloc(sizeof(struct nodeList));
  x->val = 5;
  x->next = 0;
  y = (struct nodeList*)malloc(sizeof(struct nodeList));
  y->val = 4;
  y->next = x;
  x = y;
  y = (struct nodeList*)malloc(sizeof(struct nodeList));
  y->val = 3;
  y->next = x;
  x = y;
  summ(x);
  printf("%d\n", summ(x));
  return 0;
}


