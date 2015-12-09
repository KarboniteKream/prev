void println(int num)
{
	return;
}

int main()
{
	int f1 = 1;
	int f2 = 1;
	int result = 0;

	while(f2 < 4000)
	{
		int f = f1 + f2;
		f1 = f2;
		f2 = f;

		if(f % 2 == 0)
		{
			result = result + f;
		}
	}

	println(result);

	return 0;
}
