using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Drawing;

namespace UIAdapter.Cond
{
    public abstract class Condition
    {
        #region define variables
        public const char separator = '|';
        public const char start = '{';
        public const char finish = '}';
        public static readonly String COLOR_PATTERN = "Color\\[(\\d+),\\s?(\\d+),\\s?(\\d+)\\]";
        protected static readonly String EMPTY_CELL = "EMPTY_CELL_EMPTY";

        private static Dictionary<char, Type> types = new Dictionary<char, Type>
        {
            { '&', typeof(AndCondition) },
            { 'C', typeof(ColorConditon) },
            { 'D', typeof(DateCondition) },
            { 'E', typeof(EmptyCondition) },
            { '!', typeof(NotCondition) },
            { 'N', typeof(NumberCondition) },
            { '^', typeof(OrCondition) },
            { '$', typeof(RegexpCondition) },
            { 'S', typeof(StringCondition) },
            { 'T', typeof(TrueCondtion) },
        };
        #endregion

        public Condition(string name)
        {
            this.name = name;
        }

        #region static methods for deserialize
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
        #endregion

        #region abstract methods
        protected abstract void Init(params string[] args);

        public abstract bool IsMatched(Dictionary<string, object> dic);

        #endregion

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

        public override bool IsMatched(Dictionary<string, object> dic)
        {
            return this.cond.Select(c => c.IsMatched(dic)).Aggregate(true, (s1, s2) => s1 && s2);
        }


        private List<Condition> cond;
    }

    public class ColorConditon :Condition
    {
        private Color color { get; set; }
        private bool foreground { get; set; }
        
        public ColorConditon()
            : base(null)
        {
        }

        public ColorConditon(String name, Color color, bool foreground = true)
            : base(name)
        {
            this.color = color;
            this.foreground = foreground;
        }

        public override string ToString()
        {
            return String.Format("{0} [name={1}, value={2}, foreground={3}]", GetType().Name, this.GetName(), this.color, this.foreground);
        }


        protected override void Init(params string[] args)
        {
            if (args.Length != 3)
            {
                 throw new Exception("Wrong args nubmer: " + args.Length);
            }

            SetName(args[0]);
            this.color = convertToColor(args[1]);
            this.foreground = Boolean.Parse(args[2]);
        }

        public override bool IsMatched(Dictionary<string, object> dic)
        {
            //TODO before calling this function, add implementation to method getRowWithColor on java side ( WinOperationExecutor)
            return false;
        }

        private Color convertToColor(String str)
        {
            Regex reg = new Regex(COLOR_PATTERN);
            Match matcher = reg.Match(str);
            if (matcher.Success)
            {
                return Color.FromArgb(Int32.Parse(
                    matcher.Groups[1].ToString())
                    , Int32.Parse(matcher.Groups[2].ToString())
                    , Int32.Parse(matcher.Groups[3].ToString())
                    );
            }
            else
            {
                throw new Exception("Can't parse color " + str + " via pattern " + COLOR_PATTERN);
            }
        }
    }

    public class Precision
    {
        private long period;
        private string alias;

        private Precision(String alias, long period)
        {
            this.period = period;
            this.alias = alias;
        }

        public static readonly Precision Absolute = new Precision("A", 0);
        public static readonly Precision OneDay = new Precision("d", 24 * 60 * 60 * 1000);
		public static readonly Precision OneHour = new Precision("H", 60 * 60 * 1000);
		public static readonly Precision OneMinute = new Precision("m", 60 * 1000);
		public static readonly Precision OneSecond= new Precision("s", 1000);
        public static readonly Precision OneMillisecond = new Precision("S", 1);
		
        public static Precision value(String str)
        {
            switch (str)
            {
                case "A": return Absolute;
                case "d": return OneDay;
                case "H": return OneHour;
                case "m": return OneMinute;
                case "s": return OneSecond;
                case "S": return OneMillisecond;
            }
            throw new Exception("Unknown precision: " + str);
        }

        public long getPeriod()
        {
            return this.period;
        }
    }

    public class DateCondition :RelativeCondition
    {
        //TODO this pattern used on java side. If you want to change it, don't forget change on java side in class DateCondition.
        private static readonly String PATTERN = "yyyy.MM.dd HH:mm:ss";
        private DateTime? date;
        private Precision precision;

        public DateCondition()
            : base()
        {
        }

        public DateCondition(String name, String relationString, DateTime date, Precision precision)
            : base(name, relationString)
        {
            this.date = date;
            this.precision = precision;
        }

        protected override string valueStr()
        {
            return this.date.Value.ToString(PATTERN);
        }

        protected override NullableInt compare(object otherValue)
        {
            DateTime? otherDate = convert(otherValue);
            if (otherDate == null || this.date == null)
            {
                return null;
            }
            if (equalWithPrecision(this.date, otherDate, this.precision))
            {
                return new NullableInt(0);
            }
            if (this.date < otherDate)
            {
                return new NullableInt(1);
            }
            else
            {
                return new NullableInt(-1);
            }
        }

        private bool equalWithPrecision(DateTime? v1, DateTime? v2, Precision precision)
        {
            double ms1 = v1.Value.ToUniversalTime().Subtract(new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc)).TotalMilliseconds;
            double ms2 = v2.Value.ToUniversalTime().Subtract(new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc)).TotalMilliseconds;
            if (precision == Precision.Absolute)
            {
                return ms1 == ms2;
            }
            return Math.Abs(ms1 - ms2) < precision.getPeriod();
        }

        private DateTime? convert(object obj)
        {
            if (obj.GetType().IsAssignableFrom(typeof(DateTime)))
            {
                return (DateTime) obj;
            }
            else if (obj.GetType().IsAssignableFrom(typeof(String)))
            {
                return DateTime.Parse("" + obj);
            }
            return null;
        }

        protected override void Init(params string[] args)
        {
            if (args.Length != 4)
            {
                 throw new Exception("Wrong args nubmer: " + args.Length);
            }
            SetName(args[0]);
            this.relation = Relation.value(args[1]);
            this.date = DateTime.Parse(args[2]);
            this.precision = Precision.value(args[3]);
        }

        public override string ToString()
        {
            return String.Format("{0} [name={1} {2} value={3}]", GetType().Name, GetName(), this.relation, this.date.Value.ToString(PATTERN));
        }

    }

    public class EmptyCondition : Condition
    {
        public EmptyCondition()
            : base(null)
        { }

        public EmptyCondition(string name)
            : base(name)
        {
        }

        protected override void Init(params string[] args)
        {
            if (args.Length != 1)
            {
                throw new Exception("Wrong args nubmer: " + args.Length);
            }
            SetName(args[0]);
        }

        public override string ToString()
        {
            return GetType().Name + " [name=" + GetName() + ", value=" + this.Value + "]";
        }

        public override bool IsMatched(Dictionary<string, object> dic)
        {
            String name = GetName();
            if (String.IsNullOrEmpty(name))
            {
                return true;
            }
            object value = null;
            dic.TryGetValue(name, out value);
            return value == null || ("" + value).Length == 0 || ("" + value).Equals(Tables.AbstractTable.EMPTY_CELL);
        }

        public string Value { get; private set; }
        public bool IgnoreCase { get; private set; }
    }

    public class NotCondition :Condition
    {
        private Condition cond { get; set; }

        public NotCondition()
            : base(null)
        {

        }

        public NotCondition(Condition cond)
            : base(null)
        {
            this.cond = cond;
        }

        protected override void Init(params string[] args)
        {
            if (args.Length != 1)
            {
                throw new Exception("Wrong args nubmer: " + args.Length);
            }
            this.cond = Deserialize(args[0]);
        }

        public override bool IsMatched(Dictionary<string, object> dic)
        {
            return !this.cond.IsMatched(dic);
        }

        public override HashSet<string> GetNames()
        {
            HashSet<string> res = base.GetNames();
            this.cond.GetNames().ToList().ForEach(s => res.Add(s));
            return res;
        }

        public override string ToString()
        {
            return "Not " + this.cond.ToString();
        }
    }

    public class NumberCondition :RelativeCondition
    {
        private Decimal? value;

        public NumberCondition()
            : base()
        {

        }

        public NumberCondition(String name, String relationString, Decimal dec)
            : base(name,relationString)
        {
            this.value = dec;
        }

        protected override string valueStr()
        {
            return this.value.ToString();
        }

        protected override NullableInt compare(object otherValue)
        {
            decimal otherNumber;
            bool tryParse = Decimal.TryParse("" + otherValue, out otherNumber);
            if (!tryParse || value == null)
            {
                return null;
            }
            if (this.value < otherNumber)
            {
                return new NullableInt(1);
            }
            if (this.value > otherNumber)
            {
                return new NullableInt(-1);
            }
            return new NullableInt(0);
        }

        protected override void Init(params string[] args)
        {
            if (args.Length != 3)
            {
                 throw new Exception("Wrong args nubmer: " + args.Length);
            }
            SetName(args[0]);
            base.relation = Relation.value(args[1]);
            this.value = Decimal.Parse(args[2]);
        }

        public override string ToString()
        {
            return String.Format("{0} [name={1} {2} value{3}]", GetType().Name, GetName(),relation.getSign(), value);
        }
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

        public override bool IsMatched(Dictionary<string, object> dic)
        {
            return this.cond.Select(c => c.IsMatched(dic)).Aggregate(true, (s1, s2) => s1 || s2);
        }

        private List<Condition> cond;
    }

    public class RegexpCondition :Condition
    {
        private String pattern { get; set; }
        public RegexpCondition()
            : base(null)
        {

        }

        public RegexpCondition(String name, String pattern)
            : base(name)
        {
            this.pattern = pattern;
        }

        protected override void Init(params string[] args)
        {
            if (args.Length != 2)
            {
                throw new Exception("Wrong args nubmer: " + args.Length);
            }
            SetName(args[0]);
            this.pattern = args[1];
        }

        public override bool IsMatched(Dictionary<string, object> dic)
        {
            String name = GetName();
            if (String.IsNullOrEmpty(name))
            {
                return true;
            }
            object value = null;
            dic.TryGetValue(name, out value);
            String strValue = "" + value;
            return new Regex(this.pattern).Match(strValue).Success;
        }

        public override string ToString()
        {
            return GetType().Name + "[name=" + GetName() + ", pattern=" + this.pattern + "]";
        }
    }

    public class Relation
    {
        private String sign;

        private Relation(String sign)
        {
            this.sign = sign;
        }

        public static readonly Relation LESS = new Relation("<");
        public static readonly Relation LESS_EQUAL = new Relation("<=");
        public static readonly Relation EQUAL = new Relation("==");
        public static readonly Relation GREAT_EQUAL = new Relation(">=");
        public static readonly Relation GREAT = new Relation(">");

        public String getSign()
        {
            return this.sign;
        }

        public static Relation value(String value)
        {
            switch (value)
            {
                case "<": return LESS;
                case "<=": return LESS_EQUAL;
                case "==": return EQUAL;
                case ">=": return GREAT_EQUAL;
                case ">" : return GREAT;

            }
            throw new Exception("Wrong value : " + value);
        }

    }

    public class NullableInt
    {
        public Int32 value { get; private set; }

        public NullableInt(Int32 value)
        {
            this.value = value;
        }
    }

    public abstract class RelativeCondition :Condition
    {
        protected Relation relation { get; set; }

        public RelativeCondition()
            : base(null)
        {

        }

        public RelativeCondition(String name, String relationString)
            : base(name)
        {
            this.relation = Relation.value(relationString);
        }

        public override bool IsMatched(Dictionary<string, object> dic)
        {
            String name = GetName();
            if (String.IsNullOrEmpty(name))
            {
                return true;
            }
            object value = null;
            dic.TryGetValue(name, out value);

            NullableInt compareValue = compare(value);
            if (compareValue == null)
            {
                return false;
            }
            switch (this.relation.getSign())
            {
                case "<": return compareValue.value < 0;
                case "<=": return compareValue.value <= 0;
                case "==": return compareValue.value == 0;
                case ">=": return compareValue.value >= 0;
                case ">": return compareValue.value > 0;
            }
            return false;
        }

        protected abstract String valueStr();
        protected abstract NullableInt compare(object otherValue);
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

    public class TrueCondtion :Condition 
    {
        public TrueCondtion()
            : this(null)
        {
        }

        public TrueCondtion(String name)
            : base(name)
        {
        }

        protected override void Init(params string[] args)
        {
            if (args.Length != 1)
            {
                throw new Exception("Wrong args nubmer: " + args.Length);
            }
            SetName(args[0]);
            throw new NotImplementedException();
        }

        public override bool IsMatched(Dictionary<string, object> dic)
        {
            return true;
        }

        public override string ToString()
        {
            return GetType().Name + "[name=" + GetName() + "]";
        }
    }
}
