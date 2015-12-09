typedef int integer;

float f;
int h = 0;
const int con = 1;

int main()
{
	// komentar
	f = 1.0f; f = 2.0;

	if(true == false) h = 1;
	else h = 2;

	if(false)
	{
		h = 3;
		h = con + h;
	}

	// for(int i = 0; i < 10; i = i + 1)
	// {
	// 	char c = 'c';
	// }

	return 0;
}
