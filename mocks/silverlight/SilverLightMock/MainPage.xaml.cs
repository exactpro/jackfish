using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Shapes;

namespace SilverLightMock
{
    public partial class MainPage : UserControl
    {
        public MainPage()
        {
            InitializeComponent();
            List<Person> list = new List<Person>();
            list.Add(new Person("AA"));
            list.Add(new Person("BB"));
            list.Add(new Person("CC"));

            string[] sample = { "a", "aa", "b", "bb", "c", "ac" };
            //autocompleteBox.ItemsSource = sample;
            //autocompleteBox.ItemsSource = list;
        }

        class Person
        {
            string name { get; set; }
            public Person(string name)
            {
                this.name = name;
            }
        }
    }
}
