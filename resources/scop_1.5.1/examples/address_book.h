// address_book.h - DMI - 7-9-02

class AddressBook
{
	public:
			
		char **name;
		char **address;
		int entries;
		
		AddressBook(int size);
		~AddressBook();
		
		void set_entry(int i, const char *n, const char *a);
		void dump();
		
		vertex *marshall();
		AddressBook(vertex *v);
};
