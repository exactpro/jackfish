using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Windows.Automation;
using System.Windows;
using System.Drawing;
using System.Windows.Forms;
using UIAdapter.Win32;
using System.Threading;

namespace UIAdapter
{
    public class ElementHighLighter : IDisposable
    {
        public ElementHighLighter(AutomationElement element)
        {
            this.element = element;
            if (element != null)
            {
                Rect rectangle = Rect.Empty;
                rectangle = (Rect)element.GetCurrentPropertyValue(AutomationElement.BoundingRectangleProperty, true);
                int width = 3;

                this.leftRectangle = new ScreenRectangle(new Rectangle((int)(rectangle.Left - width), (int)rectangle.Top, width, (int)rectangle.Height));
                this.topRectangle = new ScreenRectangle(new Rectangle((int)(rectangle.Left - width), (int)(rectangle.Top - width), (int)(rectangle.Width + (2 * width)), width));
                this.rightRectangle = new ScreenRectangle(new Rectangle((int)(rectangle.Left + rectangle.Width), (int)rectangle.Top, (int)width, (int)rectangle.Height));
                this.bottomRectangle = new ScreenRectangle(new Rectangle((int)(rectangle.Left - width), (int)(rectangle.Top + rectangle.Height), (int)rectangle.Width + (2 * width), width));

            }
        }

        #region IDisposable

        public void Dispose()
        {
            if (this.element != null)
            {
                this.leftRectangle.Dispose();
                this.rightRectangle.Dispose();
                this.topRectangle.Dispose();
                this.bottomRectangle.Dispose();
            }
        }

        #endregion

        private ScreenRectangle leftRectangle, bottomRectangle, rightRectangle, topRectangle;

        private AutomationElement element;
    }

    class ScreenRectangle : IDisposable
    {
        public ScreenRectangle(Rectangle location)
        {
            //initialize the form
            this.form = new Form();

            form.FormBorderStyle = FormBorderStyle.None;
            form.BackColor = Color.Red;
            form.ShowInTaskbar = false;
            form.TopMost = true;
            form.Visible = false;
            form.Left = location.Left;
            form.Top = location.Top;
            form.Width = location.Width;
            form.Height = location.Height;
            form.Hide();
            form.Opacity = 0.8;

            //set popup style
            int num1 = UnsafeNativeMethods.GetWindowLong(form.Handle, -20);
            UnsafeNativeMethods.SetWindowLong(form.Handle, -20, num1 | 0x80);

            SafeNativeMethods.SetWindowPos(this.form.Handle, NativeMethods.HWND_TOPMOST, location.X, location.Y, location.Width, location.Height, 0x10);
            Visible(true);
        }

        #region IDisposable

        public void Dispose()
        {
            Visible(false);
            this.form.Dispose();
        }

        #endregion

        private void Visible(bool show)
        {
            if (show)
            {
                SafeNativeMethods.ShowWindow(this.form.Handle, 8);
            }
            else
            {
                SafeNativeMethods.ShowWindow(this.form.Handle, 0);
            }
        }

        private Form form;
    }

}
