import { useRef } from 'react';
import { Utensils, Soup, Pizza, Fish, Coffee, Flame, Apple, Cake } from 'lucide-react';
import { cn } from '@/lib/utils';

interface CategoryNavProps {
  selectedCategory: string | null;
  onSelectCategory: (category: string | null) => void;
}

const categoryIcons: Record<string, React.ElementType> = {
  '快餐便当': Utensils,
  '面食粥点': Soup,
  '汉堡披萨': Pizza,
  '日韩料理': Fish,
  '奶茶饮品': Coffee,
  '烧烤炸串': Flame,
  '水果生鲜': Apple,
  '甜点蛋糕': Cake,
};

const categories = [
  { id: 'all', name: '全部' },
  { id: '1', name: '快餐便当' },
  { id: '2', name: '面食粥点' },
  { id: '3', name: '汉堡披萨' },
  { id: '4', name: '日韩料理' },
  { id: '5', name: '奶茶饮品' },
  { id: '6', name: '烧烤炸串' },
  { id: '7', name: '水果生鲜' },
  { id: '8', name: '甜点蛋糕' },
];

export function CategoryNav({ selectedCategory, onSelectCategory }: CategoryNavProps) {
  const scrollRef = useRef<HTMLDivElement>(null);

  return (
    <div className="relative">
      <div
        ref={scrollRef}
        className="flex gap-3 overflow-x-auto pb-2 custom-scrollbar scroll-smooth"
      >
        {categories.map((category, index) => {
          const Icon = categoryIcons[category.name];
          const isSelected = selectedCategory === category.id || 
            (selectedCategory === null && category.id === 'all');
          
          return (
            <button
              key={category.id}
              onClick={() => onSelectCategory(category.id === 'all' ? null : category.id)}
              className={cn(
                'flex flex-col items-center gap-2 min-w-[72px] p-3 rounded-2xl transition-all duration-250',
                isSelected
                  ? 'bg-orange-500 text-white shadow-primary'
                  : 'bg-white text-gray-600 hover:bg-orange-50 hover:text-orange-500 shadow-card'
              )}
              style={{ animationDelay: `${index * 50}ms` }}
            >
              {Icon && (
                <div
                  className={cn(
                    'w-10 h-10 rounded-xl flex items-center justify-center transition-colors',
                    isSelected ? 'bg-white/20' : 'bg-orange-50'
                  )}
                >
                  <Icon className={cn('w-5 h-5', isSelected ? 'text-white' : 'text-orange-500')} />
                </div>
              )}
              <span className="text-xs font-medium whitespace-nowrap">{category.name}</span>
            </button>
          );
        })}
      </div>
    </div>
  );
}
