using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace UIAdapter.Cond
{
    public abstract class Condition
    {
        public const char separator = '|';
        public const char start = '{';
        public const char finish = '}';

        public Condition(string name)
        {
            this.name = name;
        }

        enum States { BEGIN, OPEN, PARAMS, END, ERROR }

        public static Condition Deserialize(string str)
        {
            States state = States.BEGIN;
            string par = "";
            List<string> pars = new List<string>();
            int level = 0;
            char type = ' ';

            foreach (char ch in str)
            {
                switch (state)
                {
                    case States.BEGIN:
                        state = types.ContainsKey(ch) ? States.OPEN : States.ERROR;
                        type = ch;
                        break;

                    case States.OPEN:
                        state = ch == start ? States.PARAMS : States.ERROR;
                        break;

                    case States.PARAMS:
                        if (level == 0)
                        {
                            switch (ch)
                            {
                                case start:
                                    level++;
                                    par += ch;
                                    break;

                                case finish:
                                    pars.Add(par);
                                    par = "";
                                    state = States.END;
                                    break;

                                case separator:
                                    pars.Add(par);
                                    par = "";
                                    break;

                                default:
                                    par += ch;
                                    break;
                            }
                        }
                        else
                        {
                            switch (ch)
                            {
                                case start:
                                    level++;
                                    break;

                                case finish:
                                    level--;
                                    break;
                            }
                            par += ch;
                        }
                        break;

                    case States.END:
                        break;

                    case States.ERROR:
                        break;
                }
            }

            if (state == States.ERROR)
            {
                throw new Exception("Cannot parse " + str);
            }

            return Create(type, pars.ToArray());
        }

        private static Condition Create(char type, params string[] args)
        {
            Type t = types[type];
            if (t != null)
            {
                Condition instance = (Condition)Activator.CreateInstance(t);
                instance.Init(args);
                return instance;
            }

            return null;
        }

        protected abstract void Init(params string[] args);

        public abstract bool IsMatched(Dictionary<string, object> dic);

        public abstract bool IsMatched(string otherName, object otherValue);

        public virtual bool IsMatched2(string otherName, object otherValue1, object otherValue2)
        {
            return IsMatched(otherName, otherValue1);
        }

        public void SetName(string name)
        {
            this.name = name;
        }

        public virtual string GetName()
        {
            return this.name;
        }

        public virtual HashSet<string> GetNames()
        {
            return new HashSet<string>() { this.name };
        }

        private string name;

        private static Dictionary<char, Type> types = new Dictionary<char, Type>
        {
            { '&', typeof(AndCondition) },
            { '|', typeof(OrCondition) },
            { 'S', typeof(StringCondition) },
        };
    }

    public class AndCondition : Condition
    {
        public AndCondition()
            : this(null)
        { }

        public AndCondition(params Condition[] cond)
            : base(null)
        {
            this.cond = cond == null ? new List<Condition>() : new List<Condition>(cond);
        }

        protected override void Init(params string[] args)
        {
            foreach (string arg in args)
            {
                this.cond.Add(Deserialize(arg));
            }
        }

        public override string ToString()
        {
            return string.Join(" AND ", this.cond);
        }

        public override HashSet<string> GetNames()
        {
            HashSet<string> res = base.GetNames();
            this.cond.ForEach(c => c.GetNames().ToList().ForEach(e => res.Add(e)));
            return res;
        }

        public override bool IsMatched(string otherName, object otherValue)
        {
            return this.cond.Select(c => c.IsMatched(otherName, otherValue)).Aggregate(true, (s1, s2) => s1 && s2);
        }

        public override bool IsMatched(Dictionary<string, object> dic)
        {
            return this.cond.Select(c => c.IsMatched(dic)).Aggregate(true, (s1, s2) => s1 && s2);
        }


        private List<Condition> cond;
    }

    public class OrCondition : Condition
    {
        public OrCondition()
            : this(null)
        { }

        public OrCondition(params Condition[] cond)
            : base(null)
        {
            this.cond = cond == null ? new List<Condition>() : new List<Condition>(cond);
        }

        protected override void Init(params string[] args)
        {
            foreach (string arg in args)
            {
                this.cond.Add(Deserialize(arg));
            }
        }

        public override string ToString()
        {
            return string.Join(" OR ", this.cond);
        }

        public override HashSet<string> GetNames()
        {
            HashSet<string> res = base.GetNames();
            this.cond.ForEach(c => c.GetNames().ToList().ForEach(e => res.Add(e)));
            return res;
        }

        public override bool IsMatched(string otherName, object otherValue)
        {
            return this.cond.Select(c => c.IsMatched(otherName, otherValue)).Aggregate(false, (s1, s2) => s1 || s2);
        }

        public override bool IsMatched(Dictionary<string, object> dic)
        {
            return this.cond.Select(c => c.IsMatched(dic)).Aggregate(true, (s1, s2) => s1 || s2);
        }

        private List<Condition> cond;
    }

    public class StringCondition : Condition
    {
        public StringCondition()
            : base(null)
        { }

        public StringCondition(string name, string value, bool ignoreCase = false)
            : base(name)
        {
            this.Value = value;
            this.IgnoreCase = ignoreCase;
        }

        protected override void Init(params string[] args)
        {
            if (args.Length != 3)
            {
                throw new Exception("Wrong args nubmer: " + args.Length);
            }
            SetName(args[0]);
            this.Value = args[1];
            this.IgnoreCase = bool.Parse(args[2]);
        }

        public override string ToString()
        {
            return GetType().Name + " [name=" + GetName() + ", value=" + this.Value + ", ignoreCase=" + this.IgnoreCase + "]";
        }

        public override HashSet<string> GetNames()
        {
            HashSet<string> res = base.GetNames();
            res.Add(GetName());
            return res;
        }


        public override bool IsMatched(string otherName, object otherValue)
        {
            if (GetName() != otherName)
            {
                return true;
            }

            string otherStrValue = "" + otherValue;
            if (this.IgnoreCase)
            {
                return string.Equals(this.Value, otherStrValue, StringComparison.OrdinalIgnoreCase);
            }
            return string.Equals(this.Value, otherStrValue);
        }

        public override bool IsMatched(Dictionary<string, object> dic)
        {
            string name = GetName();
            if (string.IsNullOrEmpty(name))
            {
                return true;
            }

            object value = null;
            dic.TryGetValue(name, out value);
            string strValue = "" + value;

            if (this.IgnoreCase)
            {
                return string.Equals(this.Value, strValue, StringComparison.OrdinalIgnoreCase);
            }
            return string.Equals(strValue, this.Value);
        }


        public bool IgnoreCase { get; private set; }

        public string Value { get; private set; }
    }
}
